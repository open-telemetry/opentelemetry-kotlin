package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportUserCodeError

internal class GuardedClock(
    private val delegate: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: Clock = ClockImpl(),
) : Clock {

    override fun now(): Long = try {
        delegate.now()
    } catch (exc: Throwable) {
        sdkErrorHandler.reportUserCodeError(exc, "Clock.now failed, using fallback clock")
        fallback.now()
    }
}

internal fun Clock.guarded(sdkErrorHandler: SdkErrorHandler): Clock = when (this) {
    is ClockImpl, is GuardedClock -> this
    else -> GuardedClock(this, sdkErrorHandler)
}
