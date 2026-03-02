
package io.opentelemetry.kotlin.logging.export

import android.content.Context
import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.PersistedTelemetryType
import io.opentelemetry.kotlin.export.TelemetryFileSystemImpl
import io.opentelemetry.kotlin.export.getFileSystem
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath

/**
 * See [persistingLogRecordProcessor].
 */
@ExperimentalApi
@ConfigDsl
public fun LogExportConfigDsl.persistingLogRecordProcessor(
    context: Context,
    processor: LogRecordProcessor,
    exporter: LogRecordExporter,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
): LogRecordProcessor {
    return persistingLogRecordProcessorImpl(
        context = context,
        processor = processor,
        exporter = exporter,
        maxQueueSize = maxQueueSize,
        scheduleDelayMs = scheduleDelayMs,
        exportTimeoutMs = exportTimeoutMs,
        maxExportBatchSize = maxExportBatchSize,
    )
}

@ExperimentalApi
@ConfigDsl
internal fun LogExportConfigDsl.persistingLogRecordProcessorImpl(
    context: Context,
    processor: LogRecordProcessor,
    exporter: LogRecordExporter,
    clock: Clock = this.clock,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): LogRecordProcessor {
    val appContext = context.applicationContext
    val storagePath = "${appContext.cacheDir}/opentelemetry-kotlin/${PersistedTelemetryType.LOGS.directoryName}".toPath()

    val fileSystem = TelemetryFileSystemImpl(
        getFileSystem(),
        storagePath
    )
    return persistingLogRecordProcessorImpl(
        processor = processor,
        exporter = exporter,
        fileSystem = fileSystem,
        clock = clock,
        maxQueueSize = maxQueueSize,
        scheduleDelayMs = scheduleDelayMs,
        exportTimeoutMs = exportTimeoutMs,
        maxExportBatchSize = maxExportBatchSize,
        sdkErrorHandler = sdkErrorHandler,
        dispatcher = dispatcher,
    )
}
