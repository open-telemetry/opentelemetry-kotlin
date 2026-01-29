@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Creates a processor that persists telemetry before exporting it. This avoids
 * data loss if the process terminates before export completes.
 *
 * @param processors a list of processors. This MUST NOT contain exporters. It
 * should only contain processors that mutate the log record.
 * @param exporters a list of exporters. These will be invoked after telemetry has been
 * queued on disk. This may include telemetry from previous process launches.
 */
@ExperimentalApi
internal fun createPersistingLogRecordProcessor(
    processors: List<LogRecordProcessor>,
    exporters: List<LogRecordExporter>,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): LogRecordProcessor {
    return PersistingLogRecordProcessor(
        processors,
        exporters,
        maxQueueSize,
        scheduleDelayMs,
        exportTimeoutMs,
        maxExportBatchSize,
        sdkErrorHandler,
        dispatcher,
    )
}
