package io.opentelemetry.kotlin.error

import io.opentelemetry.kotlin.platform.PlatformLogLevel
import io.opentelemetry.kotlin.platform.platformLog

/**
 * An [SdkErrorHandler] that logs errors using the platform's default logging mechanism.
 */
public object PlatformSdkErrorHandler : SdkErrorHandler {

    override fun onError(error: SdkError) {
        val level = error.severity.toPlatformLogLevel()
        when (error) {
            is SdkError.ApiMisuse -> platformLog(error.toLogMessage(), level)
            is SdkError.UserCodeError -> platformLog(error.toLogMessage(), level, error.cause)
            is SdkError.SdkCodeError -> platformLog(error.toLogMessage(), level, error.cause)
            is SdkError.TransientError -> platformLog(error.toLogMessage(), level, error.cause)
        }
    }
}

internal fun SdkError.toLogMessage(): String = when (this) {
    is SdkError.ApiMisuse -> "$api misused: $message"
    is SdkError.UserCodeError -> "User code failed: $message"
    is SdkError.SdkCodeError -> "SDK code failed: $message"
    is SdkError.TransientError -> "Transient error: $message"
}

internal fun SdkErrorSeverity.toPlatformLogLevel(): PlatformLogLevel = when (this) {
    SdkErrorSeverity.INFO -> PlatformLogLevel.INFO
    SdkErrorSeverity.WARNING -> PlatformLogLevel.WARNING
    SdkErrorSeverity.ERROR -> PlatformLogLevel.ERROR
}
