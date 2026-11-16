package io.opentelemetry.kotlin.error

import io.opentelemetry.kotlin.AtomicReference

/**
 * The maximum number of errors buffered while no delegate is set.
 */
internal const val MAX_BUFFERED_ERRORS = 128

/**
 * An [SdkErrorHandler] that buffers errors until a delegate is set, then drains the buffer and
 * forwards later errors to it.
 *
 * Nothing escapes [onError] or [setDelegate]. If the delegate throws, the first failure is reported
 * to that delegate once as a [SdkError.UserCodeError]; later failures are dropped. Errors reported
 * back into this handler from inside a delegate call on the same thread are dropped, so a delegate
 * can't recurse into itself.
 */
internal class BufferingSdkErrorHandler : SdkErrorHandler {

    private sealed class State {
        class Buffering(val errors: List<SdkError>, val dropped: Int) : State()

        class Delegating(val delegate: SdkErrorHandler) : State() {
            val failure = AtomicReference<Throwable?>(null)
        }
    }

    private val empty = State.Buffering(emptyList(), 0)
    private val state = AtomicReference<State>(empty)

    override fun onError(error: SdkError) {
        try {
            route(error)
        } catch (ignored: Throwable) {
            ignored.restoreCaughtInterruptIfNeeded()
        }
    }

    /**
     * Sets [delegate] and drains the buffer into it. Returns false, and changes nothing, if a
     * delegate is already set, or if called from inside a delegate call (where the replay would
     * be dropped by the re-entrancy guard).
     */
    fun setDelegate(delegate: SdkErrorHandler): Boolean {
        val installed = State.Delegating(delegate)
        val drained = try {
            when {
                DeliveryReentrancyGuard.isActive() -> null
                else -> claim(installed)
            }
        } catch (ignored: Throwable) {
            ignored.restoreCaughtInterruptIfNeeded()
            null
        } ?: return false

        drained.errors.forEach { replay(installed) { it } }
        if (drained.dropped > 0) {
            replay(installed) { droppedSummary(drained.dropped) }
        }
        return true
    }

    /**
     * Clears any delegate and discards buffered errors.
     */
    fun reset() {
        state.set(empty)
    }

    private fun route(error: SdkError) {
        while (true) {
            when (val current = state.get()) {
                is State.Delegating -> {
                    deliver(current, error)
                    return
                }

                is State.Buffering -> {
                    val next = when {
                        current.errors.size < MAX_BUFFERED_ERRORS ->
                            State.Buffering(current.errors + error, current.dropped)

                        else -> State.Buffering(current.errors, current.dropped + 1)
                    }
                    if (state.compareAndSet(current, next)) {
                        return
                    }
                }
            }
        }
    }

    /**
     * Swaps [installed] in if no delegate is set, returning the buffer it replaced, or null.
     */
    private fun claim(installed: State.Delegating): State.Buffering? {
        while (true) {
            val current = state.get() as? State.Buffering ?: return null
            if (state.compareAndSet(current, installed)) {
                return current
            }
        }
    }

    private inline fun replay(target: State.Delegating, error: () -> SdkError) {
        try {
            deliver(target, error())
        } catch (ignored: Throwable) {
            ignored.restoreCaughtInterruptIfNeeded()
        }
    }

    private fun deliver(target: State.Delegating, error: SdkError) {
        if (DeliveryReentrancyGuard.isActive()) {
            return
        }
        DeliveryReentrancyGuard.setActive(true)
        try {
            target.delegate.onError(error)
        } catch (thrown: Throwable) {
            thrown.restoreCaughtInterruptIfNeeded()
            reportDelegateFailure(target, thrown)
        } finally {
            DeliveryReentrancyGuard.setActive(false)
        }
    }

    private fun reportDelegateFailure(target: State.Delegating, thrown: Throwable) {
        if (!target.failure.compareAndSet(null, thrown)) {
            return
        }
        try {
            target.delegate.onError(
                SdkError.UserCodeError(
                    cause = thrown,
                    message = "The process-wide SdkErrorHandler delegate threw while handling an error. " +
                        "Later failures of this delegate are not reported.",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
        } catch (ignored: Throwable) {
            ignored.restoreCaughtInterruptIfNeeded()
        }
    }

    private fun droppedSummary(dropped: Int): SdkError = SdkError.ApiMisuse(
        api = "SdkErrorHandler",
        message = "$dropped errors were reported before an SdkErrorHandler delegate was set and were dropped. " +
            "Only the first $MAX_BUFFERED_ERRORS were kept.",
        severity = SdkErrorSeverity.WARNING,
    )
}
