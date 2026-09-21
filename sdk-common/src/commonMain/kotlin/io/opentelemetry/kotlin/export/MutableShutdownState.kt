package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.AtomicBoolean
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Non-locking but thread-safe implementation of [ShutdownState]. Objects that can read but not modify
 * the shutdown state should use [ShutdownState] instead of this.
 */
@ThreadSafe
@ExperimentalApi
public class MutableShutdownState : ShutdownState() {

    private val shutdown = AtomicBoolean(false)

    override val isShutdown: Boolean
        get() = shutdown.get()

    /**
     * Perform shutdown upon invocation
     */
    public fun shutdownNow() {
        shutdown.set(true)
    }

    /**
     * If not already shut down, call [shutdownNow] and run [action] within [timeoutMs] milliseconds.
     * Returns [OperationResultCode.Failure] if the timeout elapses before [action] completes.
     * If already shut down, returns [OperationResultCode.Success].
     *
     * The shutdown transition is atomic, so if several callers race only one of them runs [action].
     */
    public suspend fun shutdown(
        timeoutMs: Long = DEFAULT_TIMEOUT_MS,
        action: suspend () -> OperationResultCode,
    ): OperationResultCode =
        if (shutdown.compareAndSet(false, true)) {
            runWithTimeout(timeoutMs, action)
        } else {
            OperationResultCode.Success
        }

    public companion object {
        public const val DEFAULT_TIMEOUT_MS: Long = 5000
    }
}
