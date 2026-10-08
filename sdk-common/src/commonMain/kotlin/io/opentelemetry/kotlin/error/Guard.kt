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
 * Use [sdkGuard] instead if [action] is SDK code.
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
): T = guardSuspend(default, action) { reportUserCodeError(it, details) }

/**
 * Runs [action], which is SDK code, such as the body of a public API method.
 *
 * If [action] throws, the failure is reported to this handler as a [SdkError.SdkCodeError] and
 * swallowed. Any user-supplied code that [action] calls should be wrapped in [userCode], so that
 * its failures are reported as a [SdkError.UserCodeError] instead.
 *
 * @param details optional description of what was being attempted, used as the error message.
 */
public inline fun SdkErrorHandler.sdkGuard(details: String? = null, action: () -> Unit) {
    try {
        action()
    } catch (exc: Throwable) {
        reportSdkCodeError(exc, details)
    }
}

/**
 * As [sdkGuard], but for SDK code that returns a value. Returns [default] if [action] throws.
 */
public inline fun <T> SdkErrorHandler.sdkGuardOrDefault(
    default: T,
    details: String? = null,
    action: () -> T,
): T = try {
    action()
} catch (exc: Throwable) {
    reportSdkCodeError(exc, details)
    default
}

/**
 * As [sdkGuardOrDefault], but for suspending code. Cancellation is handled as in
 * [guardOrDefaultSuspend].
 */
public suspend fun <T> SdkErrorHandler.sdkGuardOrDefaultSuspend(
    default: T,
    details: String? = null,
    action: suspend () -> T,
): T = guardSuspend(default, action) { reportSdkCodeError(it, details) }

/**
 * Runs [action], which is user-supplied code called from within an [sdkGuard]. Anything [action]
 * throws is tagged so that the enclosing [sdkGuard] reports it as a [SdkError.UserCodeError] rather
 * than a [SdkError.SdkCodeError]. The failure still aborts the enclosing guarded operation.
 *
 * This must only be called inside an [sdkGuard], [sdkGuardOrDefault] or
 * [sdkGuardOrDefaultSuspend], otherwise the tagged exception would escape.
 */
public inline fun <T> userCode(action: () -> T): T = try {
    action()
} catch (exc: UserCodeException) {
    throw exc
} catch (exc: Throwable) {
    throw UserCodeException(exc)
}

/**
 * Wraps a failure in user-supplied code that was thrown inside an [sdkGuard]. See [userCode].
 */
@PublishedApi
internal class UserCodeException(override val cause: Throwable) : RuntimeException(cause)

private fun Throwable.unwrapUserCode(): Throwable = (this as? UserCodeException)?.cause ?: this

private suspend inline fun <T> guardSuspend(
    default: T,
    action: suspend () -> T,
    report: (Throwable) -> Unit,
): T = try {
    action()
} catch (exc: Throwable) {
    if (exc.unwrapUserCode() is CancellationException) {
        currentCoroutineContext().ensureActive()
    }
    report(exc)
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
            exc.unwrapUserCode(),
            details ?: DEFAULT_DETAILS,
            SdkErrorSeverity.WARNING
        )
    )
}

/**
 * Reports [exc] to this handler as a [SdkError.SdkCodeError], unless it was thrown by [userCode],
 * in which case it is reported as a [SdkError.UserCodeError]. A handler that throws in response
 * must not take down the guard that called it, so any such failure is swallowed.
 */
public fun SdkErrorHandler.reportSdkCodeError(exc: Throwable, details: String?) {
    if (exc is UserCodeException) {
        reportUserCodeError(exc, details)
        return
    }
    reportError(
        SdkError.SdkCodeError(
            exc,
            details ?: DEFAULT_DETAILS,
            SdkErrorSeverity.WARNING
        )
    )
}
