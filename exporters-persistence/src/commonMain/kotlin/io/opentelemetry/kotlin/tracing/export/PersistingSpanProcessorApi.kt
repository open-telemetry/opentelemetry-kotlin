
package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.PersistedTelemetryConfig
import io.opentelemetry.kotlin.export.TelemetryFileSystem
import io.opentelemetry.kotlin.export.TelemetryFileSystemImpl
import io.opentelemetry.kotlin.export.getFileSystem
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.Path

/**
 * Creates a processor that persists telemetry before exporting it. This avoids
 * data loss if the process terminates before export completes.
 *
 * @param processor a processor. This MUST NOT call exporters. It
 * should only consist of a processor that mutates the span.
 * @param exporter an exporter. This will be invoked after telemetry has been
 * queued on disk. This may include telemetry from previous process launches.
 * @param cacheDirectory the directory to use for caching telemetry. Telemetry will be stored
 * within this location.
 *
 * This processor is not supported on JS platforms currently.
 */
@ExperimentalApi
@ConfigDsl
public fun TraceExportConfigDsl.persistingSpanProcessor(
    processor: SpanProcessor,
    exporter: SpanExporter,
    cacheDirectory: Path,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
): SpanProcessor {
    return persistingSpanProcessorImpl(
        processor = processor,
        exporter = exporter,
        fileSystem = TelemetryFileSystemImpl(getFileSystem(), cacheDirectory),
        maxQueueSize = maxQueueSize,
        scheduleDelayMs = scheduleDelayMs,
        exportTimeoutMs = exportTimeoutMs,
        maxExportBatchSize = maxExportBatchSize,
    )
}

@ExperimentalApi
@ConfigDsl
internal fun TraceExportConfigDsl.persistingSpanProcessorImpl(
    processor: SpanProcessor,
    exporter: SpanExporter,
    fileSystem: TelemetryFileSystem,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): SpanProcessor {
    return PersistingSpanProcessor(
        processor = processor,
        exporter = exporter,
        fileSystem = fileSystem,
        dsl = this,
        serializer = { it.toProtobufByteArray() },
        deserializer = { it.toSpanDataList() },
        config = PersistedTelemetryConfig(),
        maxQueueSize = maxQueueSize,
        scheduleDelayMs = scheduleDelayMs,
        exportTimeoutMs = exportTimeoutMs,
        maxExportBatchSize = maxExportBatchSize,
        sdkErrorHandler = sdkErrorHandler,
        dispatcher = dispatcher,
    )
}
