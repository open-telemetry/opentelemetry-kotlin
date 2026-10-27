package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.reportSdkCodeError
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.ioDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

private const val SHUTDOWN_TIMEOUT_MS = 5000L

internal class TelemetryExporter<T>(
    private val initialDelayMs: Long,
    private val maxAttemptIntervalMs: Long,
    private val maxAttempts: Int,
    private val sdkErrorHandler: SdkErrorHandler,
    private val coroutineContext: CoroutineContext = ioDispatcher,
    private val random: Random = Random.Default,
    private val exportAction: suspend (telemetry: List<T>) -> OtlpResponse,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val activeExports = mutableSetOf<Job>()
    private val activeExportsMutex = Mutex()

    /**
     * Exports telemetry with exponential backoff when a failure is encountered.
     */
    suspend fun export(telemetry: List<T>): OperationResultCode {
        if (shutdownState.isShutdown) {
            return Failure
        }
        if (telemetry.isEmpty()) {
            return Success
        }
        return try {
            withContext(coroutineContext) {
                val currentJob = currentCoroutineContext()[Job]!!
                val registered = activeExportsMutex.withLock {
                    if (shutdownState.isShutdown) {
                        false
                    } else {
                        activeExports.add(currentJob)
                        true
                    }
                }
                if (!registered) {
                    Failure
                } else {
                    try {
                        exportTelemetry(telemetry)
                    } finally {
                        activeExportsMutex.withLock {
                            activeExports.remove(currentJob)
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            sdkErrorHandler.reportSdkCodeError(e, "OTLP export failed")
            Failure
        }
    }

    private suspend fun exportTelemetry(telemetry: List<T>): OperationResultCode {
        var delayMs = initialDelayMs
        repeat(maxAttempts) {
            when (val response = exportAction(telemetry)) {
                is OtlpResponse.Success,
                is OtlpResponse.PartialSuccess -> {
                    return Success
                }

                is OtlpResponse.UnretryableError -> {
                    return Failure
                }

                is OtlpResponse.RetryableError -> {
                    delay((response.retryAfterMs?.coerceAtMost(maxAttemptIntervalMs) ?: jittered(delayMs)).milliseconds)
                    delayMs = (delayMs * 2).coerceAtMost(maxAttemptIntervalMs)
                }

                is OtlpResponse.Unknown -> {
                    delay(jittered(delayMs).milliseconds)
                    delayMs = (delayMs * 2).coerceAtMost(maxAttemptIntervalMs)
                }
            }
        }
        return Failure
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
            activeExportsMutex.withLock {
                activeExports.forEach { it.cancel() }
                activeExports.clear()
            }
            Success
        }
}
