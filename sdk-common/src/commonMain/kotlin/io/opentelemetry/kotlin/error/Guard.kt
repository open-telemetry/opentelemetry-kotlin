package io.opentelemetry.kotlin.error

import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_DETAILS = "Operation failed"

/**
 * Runs [action], which may be user-supplied code.
 *
 * If [action] completes normally nothing happens. If it throws, the failure is reported to this
 * handler as a [SdkError.UserCodeError] and swallowed.
 *
 * @param details optional description of what was being attempted, used as the error message.
 */
public inline fun SdkErrorHandler.guard(details: String? = null, action: () -> Unit) {
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
public inline fun <T> SdkErrorHandler.guardOrDefault(
    default: T,
    details: String? = null,
    action: () -> T,
): T = try {
    action()
} catch (exc: Throwable) {
    reportUserCodeError(exc, details)
    default
}

/**
 * As [guardOrDefault], but for suspending code. [CancellationException] is rethrown rather than
 * reported, so that coroutine cancellation keeps propagating as normal.
 */
public suspend fun <T> SdkErrorHandler.guardOrDefaultSuspend(
    default: T,
    details: String? = null,
    action: suspend () -> T,
): T = try {
    action()
} catch (exc: CancellationException) {
    throw exc
} catch (exc: Throwable) {
    reportUserCodeError(exc, details)
    default
}

/**
 * Reports [error] to this handler. A handler that throws in response must not take down the
 * caller, so any such failure is swallowed.
 */
public fun SdkErrorHandler.reportError(error: SdkError) {
    try {
        onError(error)
    } catch (ignored: Throwable) {
        // swallow
    }
}

/**
 * Reports [exc] to this handler as a [SdkError.UserCodeError]. A handler that throws in response
 * must not take down the guard that called it, so any such failure is swallowed.
 */
public fun SdkErrorHandler.reportUserCodeError(exc: Throwable, details: String?) {
    reportError(
        SdkError.UserCodeError(
            exc,
            details ?: DEFAULT_DETAILS,
            SdkErrorSeverity.WARNING
        )
    )
}
