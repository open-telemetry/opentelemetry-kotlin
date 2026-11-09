package io.opentelemetry.kotlin.error

/**
 * Reports [exc], thrown by user-supplied code that the API invoked, to the process-wide default
 * [SdkErrorHandler].
 */
internal fun reportApiUserCodeError(exc: Throwable, message: String) {
    reportError(SdkError.UserCodeError(exc, message, SdkErrorSeverity.WARNING))
}
