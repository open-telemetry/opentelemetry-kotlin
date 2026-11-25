package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportUserCodeError

private const val CLOCK_FAILED_MSG = "Clock.now failed, using fallback clock. Later failures are not reported."

internal class GuardedClock(
    private val delegate: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: Clock = ClockImpl(),
) : Clock {

    private val nowFailure = ReportOnce()

    override fun now(): Long = try {
        delegate.now()
    } catch (exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(exc, CLOCK_FAILED_MSG, nowFailure)
        fallback.now()
    }
}

internal fun Clock.guarded(sdkErrorHandler: SdkErrorHandler): Clock = when (this) {
    is ClockImpl, is GuardedClock -> this
    else -> GuardedClock(this, sdkErrorHandler)
}
