package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.export.OperationResultCode.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class TelemetryExporter<T>(
    private val initialDelayMs: Long,
    private val maxAttemptIntervalMs: Long,
    private val maxAttempts: Int,
    private val exportAction: suspend (telemetry: List<T>) -> OtlpResponse,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    /**
     * Exports telemetry via coroutines and uses exponential backoff when a failure
     * is encountered.
     */
    fun export(telemetry: List<T>): OperationResultCode =
        shutdownState.ifActive {
            scope.launch {
                exportTelemetry(telemetry)
            }
            Success
        }

    private suspend fun exportTelemetry(telemetry: List<T>) {
        var delayMs = initialDelayMs
        repeat(maxAttempts) {
            when (exportAction(telemetry)) {
                is OtlpResponse.Success -> {
                    return
                }

                is OtlpResponse.ClientError -> {
                    return
                }

                is OtlpResponse.ServerError, is OtlpResponse.Unknown -> {
                    delay(delayMs)
                    delayMs = (delayMs * 2).coerceAtMost(maxAttemptIntervalMs)
                }
            }
        }
    }

    override suspend fun forceFlush(): OperationResultCode = Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            scope.cancel()
            Success
        }
}
