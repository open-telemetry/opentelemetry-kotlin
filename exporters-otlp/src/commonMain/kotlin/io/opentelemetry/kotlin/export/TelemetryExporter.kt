package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.export.OperationResultCode.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

private const val SHUTDOWN_TIMEOUT_MS = 5000L

internal class TelemetryExporter<T>(
    private val initialDelayMs: Long,
    private val maxAttemptIntervalMs: Long,
    private val maxAttempts: Int,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    private val random: Random = Random.Default,
    private val exportAction: suspend (telemetry: List<T>) -> OtlpResponse,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + coroutineContext + telemetryExceptionHandler("OTLP exporter"))

    /**
     * Exports telemetry via coroutines and uses exponential backoff when a failure
     * is encountered.
     */
    fun export(telemetry: List<T>): OperationResultCode =
        shutdownState.ifActive {
            if (telemetry.isNotEmpty()) {
                scope.launch {
                    exportTelemetry(telemetry)
                }
            }
            Success
        }

    private suspend fun exportTelemetry(telemetry: List<T>) {
        var delayMs = initialDelayMs
        repeat(maxAttempts) {
            when (val response = exportAction(telemetry)) {
                is OtlpResponse.Success -> {
                    return
                }

                // The server accepted the request; retrying would only re-send the rejected
                // portion, so treat a partial success as terminal.
                is OtlpResponse.PartialSuccess -> {
                    return
                }

                is OtlpResponse.ClientError -> {
                    return
                }

                is OtlpResponse.RetryableError -> {
                    delay(response.retryAfterMs ?: jittered(delayMs))
                    delayMs = (delayMs * 2).coerceAtMost(maxAttemptIntervalMs)
                }

                is OtlpResponse.ServerError, is OtlpResponse.Unknown -> {
                    delay(jittered(delayMs))
                    delayMs = (delayMs * 2).coerceAtMost(maxAttemptIntervalMs)
                }
            }
        }
    }

    /**
     * Applies jitter to a backoff interval, returning a random duration in
     * `[delayMs / 2, delayMs]`. Jitter spreads out retries from many clients to avoid
     * synchronized retry storms against an overloaded server.
     */
    private fun jittered(delayMs: Long): Long {
        val half = delayMs / 2
        return half + random.nextLong(half + 1)
    }

    override suspend fun forceFlush(): OperationResultCode = Success

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(SHUTDOWN_TIMEOUT_MS) {
            scope.cancel()
            Success
        }
}
