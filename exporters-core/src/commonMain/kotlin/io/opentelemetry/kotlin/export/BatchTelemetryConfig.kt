package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.config.validateOrUseDefault
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

internal class BatchTelemetryConfig(
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    forceFlushTimeoutMs: Long = BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS,
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
) {
    /**
     * Maximum number of telemetry items the queue can hold before items are dropped.
     */
    val maxQueueSize: Int = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "maxQueueSize",
        value = maxQueueSize,
        default = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    ) { it >= 0 }

    /**
     * Delay between scheduled flushes, in milliseconds.
     */
    val scheduleDelayMs: Long = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "scheduleDelayMs",
        value = scheduleDelayMs,
        default = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    ) { it > 0 }

    /**
     * Timeout for a single export operation, in milliseconds.
     */
    val exportTimeoutMs: Long = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "exportTimeoutMs",
        value = exportTimeoutMs,
        default = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    ) { it >= 0 }

    /**
     * Maximum number of telemetry items to export per batch. Will be capped at maxQueueSize if it exceeds it.
     */
    val maxExportBatchSize: Int = if (maxExportBatchSize < 0) {
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
            default = this.maxQueueSize,
        ) { it <= this.maxQueueSize }
    }

    /**
     * Timeout for forceFlush, in milliseconds.
     */
    val forceFlushTimeoutMs: Long = validateOrUseDefault(
        sdkErrorHandler = sdkErrorHandler,
        api = API,
        configParameterName = "forceFlushTimeoutMs",
        value = forceFlushTimeoutMs,
        default = BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS,
    ) { it >= 0 }

    private companion object {
        const val API = "BatchTelemetryConfig"
    }
}
