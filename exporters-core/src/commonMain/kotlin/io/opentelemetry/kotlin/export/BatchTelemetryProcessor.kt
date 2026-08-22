package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.error.guardOrDefaultSuspend
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class BatchTelemetryProcessor<T>(
    private val config: BatchTelemetryConfig,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val exportAction: suspend (telemetry: List<T>) -> OperationResultCode,
) : TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val scope =
        CoroutineScope(
            SupervisorJob() + dispatcher + telemetryExceptionHandler("Batch processor", config.sdkErrorHandler)
        )
    private val mutex = Mutex()
    private val queue = ArrayDeque<T>()

    init {
        scope.launch {
            while (!shutdownState.isShutdown) {
                delay(config.scheduleDelayMs)
                flushInternal()
            }
        }
    }

    fun processTelemetry(telemetry: T) {
        shutdownState.execute {
            if (queue.size < config.maxQueueSize) {
                queue.add(telemetry)
            }
        }
    }

    override suspend fun forceFlush(): OperationResultCode {
        if (shutdownState.isShutdown) {
            return OperationResultCode.Success
        }
        return runWithTimeout(config.forceFlushTimeoutMs) {
            scope.launch { flushInternal() }.join()
            OperationResultCode.Success
        }
    }

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            scope.cancel()
            OperationResultCode.Success
        }

    private suspend fun flushInternal() {
        while (true) {
            val batch = mutex.withLock {
                val size = minOf(queue.size, config.maxExportBatchSize)
                List(size) { queue.removeFirst() }
            }

            if (batch.isEmpty()) {
                return
            }

            config.sdkErrorHandler.guardOrDefaultSuspend(
                OperationResultCode.Failure,
                "Batch export failed",
            ) {
                runWithTimeout(config.exportTimeoutMs) { exportAction(batch) }
            }
        }
    }
}
