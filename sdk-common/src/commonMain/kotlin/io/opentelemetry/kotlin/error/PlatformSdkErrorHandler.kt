package io.opentelemetry.kotlin.error

import io.opentelemetry.kotlin.PlatformLogLevel
import io.opentelemetry.kotlin.platformLog
import io.opentelemetry.kotlin.safeExceptionAttributes

/**
 * An [SdkErrorHandler] that writes everything reported to it to the platform log (e.g. Logcat on
 * Android). This is the default behavior when no handler is configured.
 */
public object PlatformSdkErrorHandler : SdkErrorHandler {

    override fun onError(error: SdkError) {
        val level = error.severity.toPlatformLogLevel()
        val cause = when (error) {
            is SdkError.ApiMisuse -> null
            is SdkError.UserCodeError -> error.cause
            is SdkError.SdkCodeError -> error.cause
        }
        val safeStackTrace = cause?.let { throwable ->
            try {
                safeExceptionAttributes(throwable).stackTrace
            } catch (_: Throwable) {
                null
            }
        }
        val message = error.toLogMessage()
        val fallbackMessage = safeStackTrace?.let { "$message\n$it" } ?: message
        try {
            platformLog(fallbackMessage, level)
        } catch (_: Throwable) {
            try {
                platformLog(message, level)
            } catch (_: Throwable) {
                // Logging must not let an error destabilize the host application.
            }
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
