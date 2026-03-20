
package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates a composite log record processor that delegates to a list of processors.
 */
@ExperimentalApi
public fun LogExportConfigDsl.compositeLogRecordProcessor(vararg processors: LogRecordProcessor): LogRecordProcessor {
    require(processors.isNotEmpty()) { "At least one processor must be provided" }
    return CompositeLogRecordProcessor(processors.toList(), NoopSdkErrorHandler)
}

/**
 * Creates a simple log record processor that immediately sends any telemetry to its exporter.
 */
@ExperimentalApi
public fun LogExportConfigDsl.simpleLogRecordProcessor(exporter: LogRecordExporter): LogRecordProcessor {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    return SimpleLogRecordProcessor(exporter, scope)
}

/**
 * Creates a composite log record exporter that delegates to a list of exporters.
 */
@ExperimentalApi
public fun LogExportConfigDsl.compositeLogRecordExporter(vararg exporters: LogRecordExporter): LogRecordExporter {
    require(exporters.isNotEmpty()) { "At least one exporter must be provided" }
    return CompositeLogRecordExporter(exporters.toList(), NoopSdkErrorHandler)
}

/**
 * Creates a batching processor that sends telemetry in batches.
 * See https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor
 */
@ExperimentalApi
public fun LogExportConfigDsl.batchLogRecordProcessor(
    exporter: LogRecordExporter,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): LogRecordProcessor = BatchLogRecordProcessorImpl(
    exporter,
    maxQueueSize,
    scheduleDelayMs,
    exportTimeoutMs,
    maxExportBatchSize,
    dispatcher,
)

/**
 * Creates a log record exporter that outputs log records to stdout. The destination is configurable
 * via a parameter that defaults to [println].
 *
 * This exporter is intended for debugging and learning purposes. It is not recommended for
 * production use. The output format is not standardized and can change at any time.
 */
@ExperimentalApi
public fun LogExportConfigDsl.stdoutLogRecordExporter(
    logger: (String) -> Unit = ::println
): LogRecordExporter = StdoutLogRecordExporter(logger)
