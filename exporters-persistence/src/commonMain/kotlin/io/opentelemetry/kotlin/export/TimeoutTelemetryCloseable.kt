package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * A [TelemetryCloseable] that wraps a delegate with a timeout for [forceFlush] and [shutdown].
 */
@OptIn(ExperimentalApi::class)
internal class TimeoutTelemetryCloseable(
    private val delegate: TelemetryCloseable,
    private val flushTimeoutMs: Long = 2000,
    private val shutdownTimeoutMs: Long = 5000,
) : TelemetryCloseable {

    override suspend fun forceFlush(): OperationResultCode {
        return try {
            withTimeout(flushTimeoutMs) {
                delegate.forceFlush()
            }
        } catch (e: TimeoutCancellationException) {
            OperationResultCode.Failure
        }
    }

    override suspend fun shutdown(): OperationResultCode {
        return try {
            withTimeout(shutdownTimeoutMs) {
                delegate.shutdown()
            }
        } catch (e: TimeoutCancellationException) {
            OperationResultCode.Failure
        }
    }
}
