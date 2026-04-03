package io.opentelemetry.kotlin.export

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Runs [action] within [timeoutMs] milliseconds.
 * Returns [OperationResultCode.Failure] if the timeout elapses before [action] completes.
 */
public suspend fun runWithTimeout(
    timeoutMs: Long,
    action: suspend () -> OperationResultCode,
): OperationResultCode =
    try {
        withTimeout(timeoutMs) { action() }
    } catch (e: TimeoutCancellationException) {
        OperationResultCode.Failure
    }
