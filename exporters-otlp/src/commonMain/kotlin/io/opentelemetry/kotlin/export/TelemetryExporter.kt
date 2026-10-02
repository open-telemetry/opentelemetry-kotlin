package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.ioDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

private const val EXPORT_WORKER_COUNT = 5
private const val WORKER_POLL_INTERVAL_MS = 100L
private const val SHUTDOWN_TIMEOUT_MS = 5000L
private const val CANCELLATION_TIMEOUT_MS = 500L

// Includes both pending and in-flight entries; each entry holds one List<T>.
private const val MAX_QUEUE_ENTRIES = 2048
private const val MAX_JITTER_MS = 100L
private const val ENQUEUE_TIMEOUT_MS = 1000L

private data class EndpointRetryState(
    var blockedUntil: ComparableTimeMark? = null,
    var epoch: Long = 0,
    var nextBackoffMs: Long,
    var isBackedOff: Boolean = false
) {
    val isEndpointAvailable: Boolean
        get() = blockedUntil?.hasPassedNow() ?: true
}

private enum class QueuedTelemetryState {
    Pending,
    InFlight
}

private class QueuedTelemetry<T>(
    val telemetry: List<T>,
    var attempts: Int = 0,
    var state: QueuedTelemetryState = QueuedTelemetryState.Pending
)

internal class TelemetryExporter<T>(
    private val initialDelayMs: Long,
    private val maxAttemptIntervalMs: Long,
    private val maxAttempts: Int,
    private val sdkErrorHandler: SdkErrorHandler,
    coroutineContext: CoroutineContext = ioDispatcher,
    private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
    private val random: Random = Random.Default,
    private val exportAction: suspend (telemetry: List<T>) -> OtlpResponse,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val exportJob = SupervisorJob()
    private val scope: CoroutineScope =
        CoroutineScope(ioDispatcher + coroutineContext + exportJob + telemetryExceptionHandler("OTLP exporter", sdkErrorHandler))
    private val state: EndpointRetryState = EndpointRetryState(nextBackoffMs = initialDelayMs)
    private val queue = mutableListOf<QueuedTelemetry<T>>()
    private val semaphore = Semaphore(MAX_QUEUE_ENTRIES)
    private val mutex = Mutex()

    init {
        repeat(EXPORT_WORKER_COUNT) {
            scope.launch {
                while (isActive) {
                    val shouldStop = mutex.withLock {
                        shutdownState.isShutdown && queue.isEmpty()
                    }
                    if (shouldStop) {
                        break
                    }
                    val attempted = tryExportNext()
                    if (!attempted) {
                        delay(WORKER_POLL_INTERVAL_MS)
                    }
                }
            }
        }
    }

    private suspend fun tryExportNext(): Boolean {
        // Reserve the first pending entry and capture the endpoint state under the same lock.
        val (inFlight, isBackedOff, epoch) = mutex.withLock {
            if (!state.isEndpointAvailable) {
                return false
            }

            val nextPending = queue.find { it.state == QueuedTelemetryState.Pending }
            if (nextPending == null) {
                return false
            }

            nextPending.state = QueuedTelemetryState.InFlight
            Triple(nextPending, state.isBackedOff, state.epoch)
        }

        var resultHandled = false
        try {
            // Spread requests out after an endpoint backoff.
            if (isBackedOff) {
                delay(random.nextLong(0, MAX_JITTER_MS))
            }

            mutex.withLock {
                inFlight.attempts++
            }

            val response = sdkErrorHandler.guardOrDefaultSuspend(
                OtlpResponse.Unknown,
                "OTLP export failed"
            ) {
                exportAction(inFlight.telemetry)
            }
            when (response) {
                is OtlpResponse.Success -> {
                    onExportComplete(inFlight, epoch, resetBackoff = true)
                }

                // The server accepted the request; retrying would only re-send the rejected
                // portion, so treat a partial success as terminal.
                is OtlpResponse.PartialSuccess -> {
                    onExportComplete(inFlight, epoch, resetBackoff = true)
                }

                is OtlpResponse.ClientError -> {
                    onExportComplete(inFlight, epoch, resetBackoff = false)
                }

                is OtlpResponse.RetryableError -> {
                    onExportError(inFlight, epoch, response.retryAfterMs)
                }

                is OtlpResponse.ServerError, is OtlpResponse.Unknown -> {
                    onExportError(inFlight, epoch, null)
                }
            }
            resultHandled = true
            return true
        } finally {
            if (!resultHandled) {
                // Do not leave an interrupted attempt marked as in flight.
                withContext(NonCancellable) {
                    mutex.withLock {
                        val item = queue.find { it == inFlight }
                        if (item != null && item.state == QueuedTelemetryState.InFlight) {
                            if (item.attempts < maxAttempts) {
                                item.state = QueuedTelemetryState.Pending
                            } else {
                                val removed = queue.remove(item)
                                if (removed) {
                                    semaphore.release()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun onExportComplete(telemetry: QueuedTelemetry<T>, epoch: Long, resetBackoff: Boolean) {
        mutex.withLock {
            queue.remove(telemetry)
            if (resetBackoff && epoch == state.epoch) {
                state.nextBackoffMs = initialDelayMs
                state.isBackedOff = false
            }
            semaphore.release()
        }
    }

    private suspend fun onExportError(telemetry: QueuedTelemetry<T>, epoch: Long, retryAfterMs: Long?) {
        mutex.withLock {
            if (epoch == state.epoch) {
                // Only the first failure from the current epoch advances the backoff and epoch.
                // Use the server's Retry-After when present; otherwise use the current backoff.
                val candidate = timeSource.markNow() + (retryAfterMs ?: state.nextBackoffMs).milliseconds
                // Never shorten an existing endpoint cooldown.
                state.blockedUntil = state.blockedUntil?.let { maxOf(it, candidate) } ?: candidate
                state.nextBackoffMs = (state.nextBackoffMs * 2).coerceAtMost(maxAttemptIntervalMs)
                state.isBackedOff = true
                state.epoch++
            } else {
                // A response from an older epoch may still extend the cooldown via Retry-After.
                retryAfterMs?.let { retryAfterMs ->
                    val candidate = timeSource.markNow() + retryAfterMs.milliseconds
                    state.blockedUntil = state.blockedUntil?.let { maxOf(it, candidate) } ?: candidate
                    state.isBackedOff = true
                }
            }
            if (telemetry.attempts >= maxAttempts) {
                queue.remove(telemetry)
                semaphore.release()
                return
            }
            telemetry.state = QueuedTelemetryState.Pending
        }
    }

    /**
     * Enqueues telemetry for asynchronous export.
     * Success means the telemetry was accepted, not delivered.
     */
    suspend fun export(telemetry: List<T>): OperationResultCode {
        if (shutdownState.isShutdown) {
            return Failure
        }
        if (telemetry.isEmpty()) {
            return Success
        }

        var acquired = false
        var enqueued = false
        try {
            withTimeoutOrNull(ENQUEUE_TIMEOUT_MS) {
                semaphore.acquire()
                acquired = true
            } ?: return Failure
            mutex.withLock {
                if (shutdownState.isShutdown) {
                    return Failure
                }
                queue.add(QueuedTelemetry(telemetry))
                enqueued = true
                return Success
            }
        } finally {
            if (acquired && !enqueued) {
                semaphore.release()
            }
        }
    }

    override suspend fun forceFlush(): OperationResultCode = Success

    override suspend fun shutdown(): OperationResultCode = withContext(NonCancellable) {
        mutex.withLock { shutdownState.shutdownNow() }
        // Let workers drain the queue and exit before the shutdown deadline.
        val completed = withTimeoutOrNull(SHUTDOWN_TIMEOUT_MS) {
            exportJob.complete()
            exportJob.join()
            queue.isEmpty()
        }
        if (completed != null && completed) {
            return@withContext Success
        }
        // If graceful shutdown did not succeed, cancel the workers and allow time for cleanup.
        exportJob.cancel()
        val cancelled = withTimeoutOrNull(CANCELLATION_TIMEOUT_MS) {
            exportJob.join()
            true
        } ?: false
        // Clear retained entries only after all workers have stopped accessing the queue.
        mutex.withLock {
            if (cancelled) {
                queue.clear()
            }
        }
        return@withContext Failure
    }
}
