package io.opentelemetry.kotlin.error

private const val DEFAULT_DETAILS = "Operation failed"

/**
 * Runs [action], which may be user-supplied code such as a processor callback.
 *
 * If [action] completes normally nothing happens. If it throws, the failure is reported to this
 * handler as a [SdkError.UserCodeError] and swallowed - telemetry must never destabilise the
 * host application.
 *
 * @param details optional description of what was being attempted, used as the error message.
 */
internal inline fun SdkErrorHandler.guard(details: String? = null, action: () -> Unit) {
    try {
        action()
    } catch (exc: Throwable) {
        reportUserCodeError(exc, details)
    }
}

/**
 * As [guard], but for user-supplied code that returns a value. Returns [default] if [action]
 * throws.
 */
internal inline fun <T> SdkErrorHandler.guardOrDefault(
    default: T,
    details: String? = null,
    action: () -> T,
): T = try {
    action()
} catch (exc: Throwable) {
    reportUserCodeError(exc, details)
    default
}

internal fun SdkErrorHandler.reportUserCodeError(exc: Throwable, details: String?) {
    onError(
        SdkError.UserCodeError(
            exc,
            details ?: DEFAULT_DETAILS,
            SdkErrorSeverity.WARNING
        )
    )
}
