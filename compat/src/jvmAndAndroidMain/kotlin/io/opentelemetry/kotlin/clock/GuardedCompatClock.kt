package io.opentelemetry.kotlin.clock

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault

/**
 * Wraps a user-supplied [Clock] so one that throws cannot destabilize the SDK: the failure is
 * reported to [sdkErrorHandler] and the fallback clock's reading is used instead.
 *
 * The cut-down compat `Clock` has no monotonic reading, so [OtelJavaClockWrapper] reads [now]
 * for timestamps and elapsed time alike, which is why the guard sits on it.
 */
internal class GuardedCompatClock(
    private val delegate: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: Clock = ClockAdapter(),
) : Clock {

    override fun now(): Long =
        sdkErrorHandler.guardOrDefault(fallback.now(), "Clock.now failed, using fallback clock") {
            delegate.now()
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
