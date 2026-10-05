package io.opentelemetry.kotlin.error

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_DETAILS = "Operation failed"

/**
 * Runs [action], which may be user-supplied code.
 *
 * If [action] completes normally nothing happens. If it throws, the failure is reported to this
 * handler as a [SdkError.UserCodeError] and swallowed. This includes [CancellationException]: a
 * synchronous [action] cannot observe coroutine cancellation, so one thrown here is a failure in
 * user code. Use [guardOrDefaultSuspend] if [action] suspends.
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
 * throws, including when it throws [CancellationException].
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
 * As [guardOrDefault], but for suspending code. If the calling coroutine has been cancelled, its
 * [CancellationException] is rethrown so that cancellation keeps propagating as normal. A
 * [CancellationException] thrown while the calling coroutine is still active came from user code,
 * so it is reported and [default] is returned instead.
 */
public suspend fun <T> SdkErrorHandler.guardOrDefaultSuspend(
    default: T,
    details: String? = null,
    action: suspend () -> T,
): T = try {
    action()
} catch (exc: CancellationException) {
    currentCoroutineContext().ensureActive()
    reportUserCodeError(exc, details)
    default
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
