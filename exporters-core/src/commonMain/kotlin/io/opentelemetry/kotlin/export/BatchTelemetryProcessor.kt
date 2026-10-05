package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ioDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
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
    private val flushRequests = Channel<CompletableDeferred<OperationResultCode>>(Channel.UNLIMITED)
    private val worker = scope.launch { runWorker() }

    fun processTelemetry(telemetry: T) {
        shutdownState.execute {
            queue.trySend(telemetry)
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
        return runWithTimeout(config.forceFlushTimeoutMs) { request.await() }
    }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(config.forceFlushTimeoutMs) {
            try {
                queue.close()
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
                val closed = select {
                    flushRequests.onReceive { request ->
                        request.complete(flush(emptyList()))
                        false
                    }
                    queue.onReceiveCatching { first ->
                        first.isClosed || fillAndExportBatch(first.getOrThrow())
                    }
                }
                if (closed) {
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
            generateSequence { flushRequests.tryReceive().getOrNull() }.forEach { it.complete(result) }
        }
    }

    /**
     * Collects a batch starting with [first] until it is full or scheduleDelayMs elapses, then exports it.
     * A flush request exports the partial batch early. Returns true if the queue was closed.
     */
    private suspend fun fillAndExportBatch(first: T): Boolean {
        val batch = ArrayList<T>(config.maxExportBatchSize)
        batch += first
        val deadline = scope.launch { delay(config.scheduleDelayMs.milliseconds) }
        try {
            while (true) {
                receiveAvailable(batch)
                if (batch.size >= config.maxExportBatchSize) {
                    exportBatch(batch)
                    return false
                }
                val closed: Boolean? = select {
                    flushRequests.onReceive { request ->
                        request.complete(flush(batch))
                        false
                    }
                    queue.onReceiveCatching { next ->
                        if (next.isClosed) {
                            exportBatch(batch)
                            true
                        } else {
                            batch += next.getOrThrow()
                            null
                        }
                    }
                    deadline.onJoin {
                        exportBatch(batch)
                        false
                    }
                }
                if (closed != null) {
                    return closed
                }
            }
        } finally {
            deadline.cancel()
        }
    }

    /**
     * Exports [pending] and everything currently queued, then flushes the exporter.
     */
    private suspend fun flush(pending: List<T>): OperationResultCode {
        var result = when {
            pending.isEmpty() -> OperationResultCode.Success
            else -> exportBatch(pending)
        }
        // bounded by the queue capacity so that busy producers can't starve the flush
        var remaining = config.maxQueueSize
        while (remaining > 0) {
            val batch = ArrayList<T>(minOf(remaining, config.maxExportBatchSize))
            receiveAvailable(batch, minOf(remaining, config.maxExportBatchSize))
            if (batch.isEmpty()) {
                break
            }
            remaining -= batch.size
            result = result and exportBatch(batch)
        }
        val flushResult = guardUserCode("Exporter forceFlush failed", config.forceFlushTimeoutMs, flushAction)
        return result and flushResult
    }

    /**
     * Adds queued items to [batch] without suspending until it holds [limit] items or the queue is empty.
     */
    private fun receiveAvailable(batch: MutableList<T>, limit: Int = config.maxExportBatchSize) {
        while (batch.size < limit) {
            val next = queue.tryReceive()
            if (!next.isSuccess) {
                return
            }
            batch += next.getOrThrow()
        }
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
