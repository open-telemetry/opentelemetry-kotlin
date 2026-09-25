package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.AtomicBoolean
import io.opentelemetry.kotlin.AtomicLong
import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import io.opentelemetry.kotlin.ioDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Batches telemetry into a bounded queue (dropping when full) that a single worker coroutine drains,
 * so [exportAction] is never invoked concurrently.
 */
internal class BatchTelemetryProcessor<T>(
    private val config: BatchTelemetryConfig,
    dispatcher: CoroutineDispatcher = ioDispatcher,
    private val flushAction: suspend () -> OperationResultCode = { OperationResultCode.Success },
    private val exportAction: suspend (telemetry: List<T>) -> OperationResultCode,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val scope =
        CoroutineScope(
            SupervisorJob() + dispatcher + telemetryExceptionHandler("Batch processor", config.sdkErrorHandler)
        )

    private val queue = Channel<T>(capacity = config.maxQueueSize)
    private val queueSize = AtomicLong(0)
    private val wakeup = Channel<Unit>(Channel.CONFLATED)
    private val flushRequests = Channel<CompletableDeferred<OperationResultCode>>(Channel.UNLIMITED)
    private val queueClosed = AtomicBoolean(false)
    private val worker = scope.launch { runWorker() }

    fun processTelemetry(telemetry: T) {
        shutdownState.execute {
            val size = queueSize.incrementAndGet()
            if (queue.trySend(telemetry).isFailure) {
                queueSize.decrementAndGet()
            } else if (size == 1L || size >= config.maxExportBatchSize) {
                wakeup.trySend(Unit)
            }
        }
    }

    override suspend fun forceFlush(): OperationResultCode {
        if (shutdownState.isShutdown) {
            return OperationResultCode.Success
        }
        val request = CompletableDeferred<OperationResultCode>()
        if (flushRequests.trySend(request).isFailure) {
            // closed by the worker exiting: fine once shut down, a failure otherwise
            return when {
                shutdownState.isShutdown -> OperationResultCode.Success
                else -> OperationResultCode.Failure
            }
        }
        wakeup.trySend(Unit)
        return runWithTimeout(config.forceFlushTimeoutMs) { request.await() }
    }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(config.forceFlushTimeoutMs) {
            try {
                queue.close()
                queueClosed.set(true)
                wakeup.trySend(Unit)
                worker.join()
                OperationResultCode.Success
            } finally {
                scope.cancel()
            }
        }

    private suspend fun runWorker() {
        var exitedNormally = false
        try {
            while (true) {
                val timedOut = awaitWakeup()
                val closing = queueClosed.get()
                val requests = drainFlushRequests()

                config.sdkErrorHandler.guardOrDefaultSuspend(Unit, "Batch processor worker failed") {
                    runExport(closing, timedOut, requests)
                }
                requests.forEach { it.complete(OperationResultCode.Failure) }

                if (closing) {
                    exitedNormally = true
                    return
                }
            }
        } finally {
            flushRequests.close()
            val result = when {
                exitedNormally -> OperationResultCode.Success
                else -> OperationResultCode.Failure
            }
            drainFlushRequests().forEach { it.complete(result) }
        }
    }

    private suspend fun runExport(
        closing: Boolean,
        timedOut: Boolean,
        requests: List<CompletableDeferred<OperationResultCode>>
    ) {
        val exportResult = when {
            closing -> exportQueued(Long.MAX_VALUE)
            timedOut || requests.isNotEmpty() -> exportQueued(queueSize.get())
            else -> exportQueued(Long.MAX_VALUE, fullBatchesOnly = true)
        }
        if (requests.isNotEmpty()) {
            val flushResult = guardUserCode("Exporter forceFlush failed", config.forceFlushTimeoutMs, flushAction)
            requests.forEach { it.complete(exportResult and flushResult) }
        }
    }

    /**
     * Returns true if the scheduled delay elapsed.
     */
    private suspend fun awaitWakeup(): Boolean {
        if (queueSize.get() <= 0) {
            wakeup.receive()
            return false
        }
        return withTimeoutOrNull(config.scheduleDelayMs.milliseconds) { wakeup.receive() } == null
    }

    private fun drainFlushRequests() = generateSequence { flushRequests.tryReceive().getOrNull() }.toList()

    /**
     * Exports up to [limit] queued items in batches of at most maxExportBatchSize.
     */
    private suspend fun exportQueued(limit: Long, fullBatchesOnly: Boolean = false): OperationResultCode {
        var remaining = limit
        var result: OperationResultCode = OperationResultCode.Success
        while (remaining > 0 && (!fullBatchesOnly || queueSize.get() >= config.maxExportBatchSize)) {
            val batch = nextBatch(minOf(remaining, config.maxExportBatchSize.toLong()).toInt())
            if (batch.isEmpty()) {
                break
            }
            remaining -= batch.size
            result = result and exportBatch(batch)
        }
        return result
    }

    private fun nextBatch(limit: Int): List<T> {
        val batch = ArrayList<T>(limit)
        while (batch.size < limit) {
            val item = queue.tryReceive()
            if (!item.isSuccess) {
                break
            }
            queueSize.decrementAndGet()
            batch += item.getOrThrow()
        }
        return batch
    }

    private suspend fun exportBatch(batch: List<T>): OperationResultCode =
        guardUserCode("Batch export failed", config.exportTimeoutMs) { exportAction(batch) }

    private suspend fun guardUserCode(
        details: String,
        timeoutMs: Long,
        action: suspend () -> OperationResultCode,
    ): OperationResultCode = config.sdkErrorHandler.guardExporterCode(details) { runWithTimeout(timeoutMs, action) }
}

internal infix fun OperationResultCode.and(other: OperationResultCode): OperationResultCode = when {
    this == OperationResultCode.Success && other == OperationResultCode.Success -> OperationResultCode.Success
    else -> OperationResultCode.Failure
}
