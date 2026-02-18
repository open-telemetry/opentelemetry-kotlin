@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import android.content.Context
import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.TelemetryFileSystemImpl
import io.opentelemetry.kotlin.export.getFileSystem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath

/**
 * See [createPersistingLogRecordProcessor].
 */
@ExperimentalApi
internal fun createPersistingLogRecordProcessor(
    context: Context,
    processors: List<LogRecordProcessor>,
    exporters: List<LogRecordExporter>,
    clock: Clock,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): LogRecordProcessor {
    val appContext = context.applicationContext
    val storagePath = "${appContext.cacheDir}/opentelemetry-kotlin/logs".toPath()

    val fileSystem = TelemetryFileSystemImpl(
        getFileSystem(),
        storagePath
    )
    return createPersistingLogRecordProcessor(
        processors = processors,
        exporters = exporters,
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
