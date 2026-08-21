package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.math.min
import kotlin.random.Random

private const val SHUTDOWN_TIMEOUT_MS = 5000L
private const val MAX_RETRY_AFTER_MS = 60_000L
private const val MAX_ATTEMPTS = 4

internal class TelemetryExporter<T>(
    private val initialDelayMs: Long,
    private val maxAttemptIntervalMs: Long,
    private val maxAttempts: Int,
    private val sdkErrorHandler: SdkErrorHandler,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    private val random: Random = Random.Default,
    private val exportAction: suspend (telemetry: List<T>) -> OtlpResponse,
    private val shutdownAction: suspend () -> Unit,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val scope: CoroutineScope =
        CoroutineScope(
            SupervisorJob() + coroutineContext + telemetryExceptionHandler(
                "OTLP exporter",
                sdkErrorHandler
            )
        )

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
        val effectiveMaxAttemptsIntervalMs = min(maxAttemptIntervalMs, MAX_RETRY_AFTER_MS)
        val effectiveMaxAttempts = min(maxAttempts, MAX_ATTEMPTS)
        repeat(effectiveMaxAttempts) {
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
                    delay(
                        (response.retryAfterMs ?: jittered(delayMs)).coerceAtMost(
                            effectiveMaxAttemptsIntervalMs
                        )
                    )
                    delayMs = (delayMs * 2).coerceAtMost(effectiveMaxAttemptsIntervalMs)
                }

                is OtlpResponse.ServerError, is OtlpResponse.Unknown -> {
                    delay(jittered(delayMs))
                    delayMs = (delayMs * 2).coerceAtMost(effectiveMaxAttemptsIntervalMs)
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
            shutdownAction()
            Success
        }
}
