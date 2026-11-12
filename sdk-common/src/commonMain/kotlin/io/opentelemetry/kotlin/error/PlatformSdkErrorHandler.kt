package io.opentelemetry.kotlin.error

import io.opentelemetry.kotlin.PlatformLogLevel
import io.opentelemetry.kotlin.platformLog

/**
 * The default [SdkErrorHandler], which logs the first occurrence of each error pattern in full
 * and periodically summarizes repetitions to avoid flooding the host's platform log.
 * Custom error handlers still receive every error without rate limiting.
 */
public object PlatformSdkErrorHandler : SdkErrorHandler {
    private val limiter = DefaultErrorLogLimiter()

    override fun onError(error: SdkError) {
        try {
            when (val decision = limiter.record(error)) {
                DefaultErrorLogLimiter.Decision.Full -> {
                    val level = error.severity.toPlatformLogLevel()
                    when (error) {
                        is SdkError.ApiMisuse -> platformLog(error.toLogMessage(), level)
                        is SdkError.UserCodeError -> platformLog(error.toLogMessage(), level, error.cause)
                        is SdkError.SdkCodeError -> platformLog(error.toLogMessage(), level, error.cause)
                    }
                }
                DefaultErrorLogLimiter.Decision.Skip -> {
                    Unit
                }
                is DefaultErrorLogLimiter.Decision.Repeated -> {
                    platformLog(
                        "${error.toLogMessage()} (repeated ${decision.count} times; subsequent occurrences suppressed)",
                        error.severity.toPlatformLogLevel(),
                    )
                }
                is DefaultErrorLogLimiter.Decision.Overflow -> {
                    platformLog(
                        "SDK error logger reached its 64-pattern limit; ${decision.count} additional errors suppressed",
                        PlatformLogLevel.WARNING,
                    )
                }
            }
        } catch (ignored: Throwable) {
            ignored.restoreInterruptIfNeeded()
            // Logging should never disrupt the host application.
        }
    }
}

internal fun SdkError.toLogMessage(): String = when (this) {
    is SdkError.ApiMisuse -> "$api misused: $message"
    is SdkError.UserCodeError -> "User code failed: $message"
    is SdkError.SdkCodeError -> "SDK code failed: $message"
}

internal fun SdkErrorSeverity.toPlatformLogLevel(): PlatformLogLevel = when (this) {
    SdkErrorSeverity.INFO -> PlatformLogLevel.INFO
    SdkErrorSeverity.WARNING -> PlatformLogLevel.WARNING
    SdkErrorSeverity.ERROR -> PlatformLogLevel.ERROR
}
