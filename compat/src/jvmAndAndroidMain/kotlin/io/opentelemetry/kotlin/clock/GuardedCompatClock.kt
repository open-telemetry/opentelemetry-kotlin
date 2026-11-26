package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ReportOnce
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportUserCodeError

private const val CLOCK_FAILED_MSG = "Clock.now failed, using fallback clock. Later failures are not reported."

/**
 * Wraps a user-supplied [Clock] so one that throws cannot destabilize the SDK: the first failure is
 * reported to [sdkErrorHandler] and the fallback clock's reading is used instead. The fallback is
 * only read after [delegate] throws.
 *
 * The cut-down compat `Clock` has no monotonic reading, so [OtelJavaClockWrapper] reads [now]
 * for timestamps and elapsed time alike, which is why the guard sits on it.
 */
internal class GuardedCompatClock(
    private val delegate: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: Clock = ClockAdapter(),
) : Clock {

    private val nowFailure = ReportOnce()

    override fun now(): Long = try {
        delegate.now()
    } catch (exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(exc, CLOCK_FAILED_MSG, nowFailure)
        fallback.now()
    }
}

/**
 * Wraps [this] when it is user-supplied, so its failures are reported rather than escaping into
 * the Java SDK. Clocks the SDK itself adapted are returned unwrapped.
 */
internal fun Clock.guarded(sdkErrorHandler: SdkErrorHandler): Clock = when (this) {
    is ClockAdapter, is GuardedCompatClock -> this
    else -> GuardedCompatClock(this, sdkErrorHandler)
}
