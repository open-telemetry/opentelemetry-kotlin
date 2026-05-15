package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.config.validateOrUseDefault
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
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

internal class BatchTelemetryProcessor<T>(
    maxQueueSize: Int,
    scheduleDelayMs: Long,
    exportTimeoutMs: Long,
    maxExportBatchSize: Int,
    forceFlushTimeoutMs: Long = BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    private val exportAction: suspend (telemetry: List<T>) -> OperationResultCode,
) : TelemetryCloseable {

    private val maxQueueSize: Int = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "maxQueueSize",
        value = maxQueueSize,
        default = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    ) { it >= 0 }

    private val scheduleDelayMs: Long = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "scheduleDelayMs",
        value = scheduleDelayMs,
        default = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    ) { it > 0 }

    private val exportTimeoutMs: Long = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "exportTimeoutMs",
        value = exportTimeoutMs,
        default = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    ) { it >= 0 }

    private val maxExportBatchSize: Int = if (maxExportBatchSize < 0) {
        validateOrUseDefault(
            sdkErrorHandler = sdkErrorHandler,
            api = API,
            configParameterName = "maxExportBatchSize",
            value = maxExportBatchSize,
            default = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
        ) { it >= 0 }
    } else {
        validateOrUseDefault(
            sdkErrorHandler = sdkErrorHandler,
            api = API,
            configParameterName = "maxExportBatchSize",
            value = maxExportBatchSize,
            default = maxQueueSize,
        ) { it <= maxQueueSize }
    }

    private val forceFlushTimeoutMs: Long = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "forceFlushTimeoutMs",
        value = forceFlushTimeoutMs,
        default = BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS,
    ) { it >= 0 }

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private val queue = mutableListOf<T>()

    init {
        scope.launch {
            while (!shutdownState.isShutdown) {
                delay(this@BatchTelemetryProcessor.scheduleDelayMs)
                flushInternal()
            }
        }
    }

    fun processTelemetry(telemetry: T) {
        shutdownState.execute {
            if (queue.size <= maxQueueSize) {
                queue.add(telemetry)
            }
        }
    }

    override suspend fun forceFlush(): OperationResultCode {
        if (shutdownState.isShutdown) {
            return OperationResultCode.Success
        }
        return runWithTimeout(forceFlushTimeoutMs) {
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

    private companion object {
        const val API = "BatchTelemetryProcessor"
    }
}
