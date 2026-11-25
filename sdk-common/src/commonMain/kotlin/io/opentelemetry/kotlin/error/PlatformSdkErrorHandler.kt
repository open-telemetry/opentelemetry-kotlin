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
        val logger = { message: String, logLevel: PlatformLogLevel ->
            platformLog(message, logLevel)
        }
        when (error) {
            is SdkError.ApiMisuse -> safelyPlatformLog(error.toLogMessage(), level, logger = logger)
            is SdkError.UserCodeError -> safelyPlatformLog(error.toLogMessage(), level, error.cause, logger)
            is SdkError.SdkCodeError -> safelyPlatformLog(error.toLogMessage(), level, error.cause, logger)
        }
    }
}

internal fun safelyPlatformLog(
    message: String,
    level: PlatformLogLevel,
    cause: Throwable? = null,
    logger: (String, PlatformLogLevel) -> Unit,
) {
    val messageWithCause = cause?.let { safeExceptionAttributes(it).stacktrace }
        ?.let { "$message\n$it" }
        ?: message
    try {
        logger(messageWithCause, level)
    } catch (_: Throwable) {
        if (cause != null) {
            try {
                logger(message, level)
            } catch (_: Throwable) {
                // The default error handler must never destabilize the host application.
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
