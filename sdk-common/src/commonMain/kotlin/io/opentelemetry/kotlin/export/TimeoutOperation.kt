package io.opentelemetry.kotlin.export

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * Runs [action] within [timeoutMs] milliseconds.
 * Returns [OperationResultCode.Timeout], a kind of [OperationResultCode.Failure], if the timeout
 * elapses before [action] completes. Other results of [action] are returned unchanged.
 */
public suspend fun runWithTimeout(
    timeoutMs: Long,
    action: suspend () -> OperationResultCode,
): OperationResultCode =
    try {
        withTimeout(timeoutMs) { action() }
    } catch (e: TimeoutCancellationException) {
        OperationResultCode.Timeout
    }
