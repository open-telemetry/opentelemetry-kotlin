package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.runWithTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Default time to wait for an operation on a wrapped opentelemetry-java component to complete.
 */
internal const val COMPAT_DEFAULT_TIMEOUT_MS: Long = 5000

/**
 * Invokes [action] on the wrapped opentelemetry-java component and suspends until the returned
 * result completes, or until [timeoutMs] elapses.
 */
internal suspend fun awaitOperationResultCode(
    timeoutMs: Long = COMPAT_DEFAULT_TIMEOUT_MS,
    action: () -> OtelJavaCompletableResultCode,
): OperationResultCode = runWithTimeout(timeoutMs) {
    action().toOperationResultCode()
}

/**
 * Suspends until this result completes, then maps it to an [OperationResultCode]. If the result
 * failed exceptionally, its failure throwable is thrown instead.
 */
internal suspend fun OtelJavaCompletableResultCode.toOperationResultCode(): OperationResultCode =
    suspendCancellableCoroutine { continuation ->
        whenComplete {
            val failure = failureThrowable
            when {
                isSuccess -> continuation.resume(OperationResultCode.Success)
                failure != null -> continuation.resumeWithException(failure)
                else -> continuation.resume(OperationResultCode.Failure)
            }
        }
    }

/**
 * Launches [action] in this scope and returns an [OtelJavaCompletableResultCode] that completes
 * with its result. Failures thrown by [action] are reported to [sdkErrorHandler]. The result fails
 * if [action] throws, exceeds [timeoutMs], or the scope is cancelled before it completes.
 */
internal fun CoroutineScope.launchAsCompletableResultCode(
    sdkErrorHandler: SdkErrorHandler,
    details: String,
    timeoutMs: Long = COMPAT_DEFAULT_TIMEOUT_MS,
    action: suspend () -> OperationResultCode,
): OtelJavaCompletableResultCode {
    val result = OtelJavaCompletableResultCode()
    val job = launch {
        val code = sdkErrorHandler.guardOrDefaultSuspend(OperationResultCode.Failure, details) {
            runWithTimeout(timeoutMs, action)
        }
        when (code) {
            OperationResultCode.Success -> result.succeed()
            else -> result.fail()
        }
    }
    job.invokeOnCompletion {
        if (!result.isDone) {
            result.fail()
        }
    }
    return result
}
