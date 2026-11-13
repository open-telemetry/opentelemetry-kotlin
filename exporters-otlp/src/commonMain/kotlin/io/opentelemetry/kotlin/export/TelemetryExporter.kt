package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.ioDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
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
private const val DRAIN_TIMEOUT_MS = 5000L
private const val CANCELLATION_TIMEOUT_MS = 500L

// Includes both pending and in-flight entries; each entry holds one List<T>.
internal const val MAX_QUEUE_ENTRIES = 2048
private const val MAX_JITTER_MS = 100L

private data class EndpointRetryState(
    var blockedUntil: ComparableTimeMark? = null,
    var epoch: Long = 0,
    var nextBackoffMs: Long,
    var isBackedOff: Boolean = false
)

private enum class QueuedTelemetryState {
    Pending,
    InFlight
}

private class QueuedTelemetry<T>(
    val telemetry: List<T>,
    val sequence: Long,
    var attempts: Int = 0,
    var state: QueuedTelemetryState = QueuedTelemetryState.Pending
)

/**
 * An in-progress forceFlush call that waits for every entry with a sequence below [endSequence].
 */
private class FlushWaiter(val endSequence: Long) {
    val result = CompletableDeferred<Boolean>()
    var failed = false
}

private sealed interface WorkerAction<out T> {
    data class Export<T>(
        val entry: QueuedTelemetry<T>,
        val epoch: Long,
        val applyJitter: Boolean,
    ) : WorkerAction<T>

    data class WaitUntil(
        val deadline: ComparableTimeMark,
    ) : WorkerAction<Nothing>

    data object WaitForWorkAvailable : WorkerAction<Nothing>
    data object Shutdown : WorkerAction<Nothing>
}

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

    // Parent job used to await or cancel all export workers.
    private val exportJob = SupervisorJob()
    private val scope: CoroutineScope =
        CoroutineScope(ioDispatcher + coroutineContext + exportJob + telemetryExceptionHandler("OTLP exporter", sdkErrorHandler))

    // Shared endpoint backoff state.
    private val state: EndpointRetryState = EndpointRetryState(nextBackoffMs = initialDelayMs)

    // Retains entries until success, terminal failure, or retry exhaustion.
    private val queue = mutableListOf<QueuedTelemetry<T>>()

    // Sequence assigned to the next enqueued entry.
    private var nextSequence = 0L

    // forceFlush calls waiting for earlier entries to finish.
    private val flushWaiters = mutableListOf<FlushWaiter>()

    // Limits queued entries, including those currently in flight.
    private val semaphore = Semaphore(MAX_QUEUE_ENTRIES)

    // Wakes idle workers; redundant signals are skipped when the buffer is full.
    private val workAvailable = Channel<Unit>(EXPORT_WORKER_COUNT)

    // Protects queue mutations, sequence numbering, flush waiters, endpoint state, and shutdown transitions.
    private val mutex = Mutex()

    init {
        repeat(EXPORT_WORKER_COUNT) {
            scope.launch {
                while (isActive) {
                    when (val action = nextAction()) {
                        is WorkerAction.Export<T> -> {
                            attemptExportEntry(action)
                        }

                        is WorkerAction.WaitUntil -> {
                            // Another request may extend the endpoint cooldown while this worker sleeps.
                            // Recheck the endpoint state on the next iteration.
                            delay(action.deadline - timeSource.markNow())
                        }

                        WorkerAction.WaitForWorkAvailable -> {
                            if (workAvailable.receiveCatching().isClosed) {
                                return@launch
                            }
                        }

                        WorkerAction.Shutdown -> {
                            return@launch
                        }
                    }
                }
            }
        }
    }

    private suspend fun nextAction(): WorkerAction<T> {
        mutex.withLock {
            if (shutdownState.isShutdown && queue.isEmpty()) {
                // The queue has drained during shutdown. Close the channel so waiting workers can exit too.
                workAvailable.close()
                return WorkerAction.Shutdown
            }

            val nextPending = queue.find { it.state == QueuedTelemetryState.Pending }
            if (nextPending == null) {
                return WorkerAction.WaitForWorkAvailable
            }

            val blockedUntil = state.blockedUntil
            if (blockedUntil != null && blockedUntil.hasNotPassedNow()) {
                return WorkerAction.WaitUntil(blockedUntil)
            }

            // Reserve the entry and capture the retry state atomically.
            // Other workers skip in-flight entries, so the export can run outside the mutex.
            nextPending.state = QueuedTelemetryState.InFlight
            return WorkerAction.Export(nextPending, state.epoch, state.isBackedOff)
        }
    }

    private suspend fun attemptExportEntry(action: WorkerAction.Export<T>) {
        val entry = action.entry
        val epoch = action.epoch
        var resultHandled = false
        try {
            // Spread requests out after an endpoint backoff.
            if (action.applyJitter) {
                delay(random.nextLong(0, MAX_JITTER_MS))
            }

            mutex.withLock {
                entry.attempts++
            }

            val response = sdkErrorHandler.guardOrDefaultSuspend(
                OtlpResponse.Unknown,
                "OTLP export failed"
            ) {
                exportAction(entry.telemetry)
            }
            when (response) {
                is OtlpResponse.Success -> {
                    onExportComplete(entry, epoch, delivered = true)
                }

                // The server accepted the request; retrying would only re-send the rejected
                // portion, so treat a partial success as terminal.
                is OtlpResponse.PartialSuccess -> {
                    onExportComplete(entry, epoch, delivered = true)
                }

                is OtlpResponse.UnretryableError -> {
                    onExportComplete(entry, epoch, delivered = false)
                }

                is OtlpResponse.RetryableError -> {
                    onExportError(entry, epoch, response.retryAfterMs?.coerceAtMost(maxAttemptIntervalMs))
                }

                is OtlpResponse.Unknown -> {
                    onExportError(entry, epoch, null)
                }
            }
            resultHandled = true
        } finally {
            if (!resultHandled) {
                // Cancellation may interrupt jitter, export, or result handling.
                // Restore or remove the reserved entry so it cannot remain in flight indefinitely.
                withContext(NonCancellable) {
                    mutex.withLock {
                        val item = queue.find { it == entry }
                        if (item != null && item.state == QueuedTelemetryState.InFlight) {
                            if (item.attempts < maxAttempts) {
                                item.state = QueuedTelemetryState.Pending
                                workAvailable.trySend(Unit)
                            } else {
                                removeEntry(item, delivered = false)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Completes an entry. A delivered entry from the current backoff epoch also resets the endpoint backoff.
     */
    private suspend fun onExportComplete(telemetry: QueuedTelemetry<T>, epoch: Long, delivered: Boolean) {
        mutex.withLock {
            if (delivered && epoch == state.epoch) {
                state.nextBackoffMs = initialDelayMs
                state.isBackedOff = false
            }
            removeEntry(telemetry, delivered)
        }
    }

    /**
     * Removes [entry] if it is still queued, and does nothing otherwise.
     * If the entry wasn't [delivered], every waiter tracking it is marked as failed. Waiters whose
     * [FlushWaiter.endSequence] is at or below the oldest queued sequence are then completed.
     */
    private fun removeEntry(entry: QueuedTelemetry<T>, delivered: Boolean) {
        if (!queue.remove(entry)) {
            return
        }
        semaphore.release()
        if (!delivered) {
            flushWaiters.forEach { waiter ->
                if (entry.sequence < waiter.endSequence) {
                    waiter.failed = true
                }
            }
        }
        val oldestSequence = queue.firstOrNull()?.sequence ?: nextSequence
        flushWaiters.forEach { waiter ->
            if (oldestSequence >= waiter.endSequence) {
                waiter.result.complete(!waiter.failed)
            }
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
                removeEntry(telemetry, delivered = false)
                return
            }
            telemetry.state = QueuedTelemetryState.Pending
            workAvailable.trySend(Unit)
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

        // Drop rather than wait when the queue is full: waiting would stall the caller
        // (e.g. the batch worker) while the endpoint is backed off.
        if (!semaphore.tryAcquire()) {
            return Failure
        }
        var enqueued = false
        try {
            mutex.withLock {
                if (shutdownState.isShutdown) {
                    return Failure
                }
                queue.add(QueuedTelemetry(telemetry, nextSequence++))
                enqueued = true
                workAvailable.trySend(Unit)
                return Success
            }
        } finally {
            if (!enqueued) {
                semaphore.release()
            }
        }
    }

    /**
     * Waits until every entry enqueued before this call is delivered, rejected with an unretryable error,
     * out of retries, or discarded at shutdown.
     *
     * Returns [Failure] if any of those entries was not delivered, or if they don't finish within
     * [DRAIN_TIMEOUT_MS]. Returns [Failure] immediately if the endpoint is backed off past that deadline
     * while an entry is still waiting to be sent. Partial success counts as delivered.
     */
    override suspend fun forceFlush(): OperationResultCode {
        val deadline = timeSource.markNow() + DRAIN_TIMEOUT_MS.milliseconds
        val waiter = mutex.withLock {
            if (queue.isEmpty()) {
                return Success
            }
            // pending entries can't be sent before the deadline while the endpoint is blocked
            val blockedUntil = state.blockedUntil
            if (blockedUntil != null && blockedUntil >= deadline &&
                queue.any { it.state == QueuedTelemetryState.Pending }
            ) {
                return Failure
            }
            FlushWaiter(nextSequence).also { flushWaiters.add(it) }
        }
        try {
            val delivered = withTimeoutOrNull(deadline - timeSource.markNow()) {
                waiter.result.await()
            } ?: false
            return if (delivered) {
                Success
            } else {
                Failure
            }
        } finally {
            withContext(NonCancellable) {
                mutex.withLock {
                    flushWaiters.remove(waiter)
                }
            }
        }
    }

    override suspend fun shutdown(): OperationResultCode {
        val deadline = timeSource.markNow() + DRAIN_TIMEOUT_MS.milliseconds
        val canDrain = withContext(NonCancellable) {
            mutex.withLock {
                shutdownState.shutdownNow()
                if (queue.isEmpty()) {
                    // If the queue is already empty, close the channel to wake idle workers so they can exit.
                    workAvailable.close()
                }
                canDrainBefore(deadline)
            }
        }
        var drained = false
        try {
            exportJob.complete()
            if (canDrain) {
                drained = withTimeoutOrNull(deadline - timeSource.markNow()) {
                    exportJob.join()
                    mutex.withLock { queue.isEmpty() }
                } ?: false
            }
        } finally {
            if (!drained) {
                withContext(NonCancellable) {
                    cancelWorkersAndClearQueue()
                }
            }
        }
        return if (drained) {
            Success
        } else {
            Failure
        }
    }

    private fun canDrainBefore(deadline: ComparableTimeMark): Boolean {
        val blockedUntil = state.blockedUntil ?: return true
        val anyInFlight = queue.any { it.state == QueuedTelemetryState.InFlight }
        return queue.isEmpty() || anyInFlight || blockedUntil < deadline
    }

    private suspend fun cancelWorkersAndClearQueue() {
        exportJob.cancel()
        val cancelled = withTimeoutOrNull(CANCELLATION_TIMEOUT_MS) {
            exportJob.join()
            true
        } ?: false
        // Clear retained entries only after all workers have stopped accessing the queue.
        if (cancelled) {
            mutex.withLock {
                queue.clear()
                flushWaiters.forEach { waiter ->
                    waiter.result.complete(false)
                }
            }
        }
    }
}
