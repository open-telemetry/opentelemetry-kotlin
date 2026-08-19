package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.runWithTimeout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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
    val resultCode = try {
        action()
    } catch (e: CancellationException) {
        throw e
    } catch (ignored: Throwable) {
        return@runWithTimeout OperationResultCode.Failure
    }
    resultCode.toOperationResultCode()
}

/**
 * Suspends until this result completes, then maps it to an [OperationResultCode].
 */
internal suspend fun OtelJavaCompletableResultCode.toOperationResultCode(): OperationResultCode =
    suspendCancellableCoroutine { continuation ->
        whenComplete {
            continuation.resume(
                when {
                    isSuccess -> OperationResultCode.Success
                    else -> OperationResultCode.Failure
                }
            )
        }
    }
