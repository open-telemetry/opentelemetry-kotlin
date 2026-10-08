package io.opentelemetry.kotlin.error

import io.opentelemetry.kotlin.platformLog

/**
 * An [SdkErrorHandler] that writes everything reported to it to the platform log (e.g. Logcat on
 * Android). This is the default behavior when no handler is configured.
 */
public object PlatformSdkErrorHandler : SdkErrorHandler {

    override fun onError(error: SdkError) {
        when (error) {
            is SdkError.ApiMisuse -> platformLog(error.toLogMessage(), error.severity)
            is SdkError.UserCodeError -> platformLog(error.toLogMessage(), error.severity, error.cause)
            is SdkError.SdkCodeError -> platformLog(error.toLogMessage(), error.severity, error.cause)
        }
    }
}

internal fun SdkError.toLogMessage(): String = when (this) {
    is SdkError.ApiMisuse -> "$api misused: $message"
    is SdkError.UserCodeError -> "User code failed: $message"
    is SdkError.SdkCodeError -> "SDK code failed: $message"
}
