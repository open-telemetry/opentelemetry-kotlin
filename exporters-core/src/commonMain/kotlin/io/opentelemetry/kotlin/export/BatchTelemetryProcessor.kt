package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.concurrent.Volatile

@OptIn(ExperimentalApi::class)
internal class BatchTelemetryProcessor<T>(
    private val maxQueueSize: Int,
    private val scheduleDelayMs: Long,
    private val exportTimeoutMs: Long,
    private val maxExportBatchSize: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val exportAction: suspend (telemetry: List<T>) -> OperationResultCode,
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private val queue = mutableListOf<T>()

    @Volatile
    private var running = true

    init {
        require(scheduleDelayMs > 0)
        require(maxQueueSize >= 0)
        require(maxExportBatchSize >= 0)
        require(exportTimeoutMs >= 0)
        require(maxExportBatchSize <= maxQueueSize)

        scope.launch {
            while (running) {
                delay(scheduleDelayMs)
                flushInternal()
            }
        }
    }

    fun processTelemetry(telemetry: T) {
        scope.launch {
            mutex.withLock {
                if (queue.size <= maxQueueSize) {
                    queue.add(telemetry)
                }
            }
        }
    }

    suspend fun forceFlush(): OperationResultCode {
        scope.launch {
            flushInternal()
        }
        return OperationResultCode.Success
    }

    fun shutdown(): OperationResultCode {
        running = false
        scope.cancel()
        return OperationResultCode.Success
    }

    private suspend fun flushInternal() {
        while (queue.isNotEmpty()) {
            val batch = mutableListOf<T>()
            mutex.withLock {
                val size = minOf(queue.size, maxExportBatchSize)
                repeat(size) { batch += queue.removeAt(0) }
            }

            if (batch.isNotEmpty()) {
                try {
                    withTimeout(exportTimeoutMs) {
                        exportAction(batch)
                    }
                } catch (ignored: Throwable) {
                    // drop, continue as normal.
                }
            }
        }
    }
}
