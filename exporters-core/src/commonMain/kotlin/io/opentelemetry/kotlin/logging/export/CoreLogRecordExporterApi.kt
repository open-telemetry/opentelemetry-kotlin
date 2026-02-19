@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates a composite log record processor that delegates to a list of processors.
 */
@ExperimentalApi
@ConfigDsl
public fun LogExportConfigDsl.compositeLogRecordProcessor(vararg processors: LogRecordProcessor): LogRecordProcessor {
    require(processors.isNotEmpty()) { "At least one processor must be provided" }
    @Suppress("DEPRECATION")
    return createCompositeLogRecordProcessor(processors.toList())
}

@ExperimentalApi
@Deprecated("Deprecated.", ReplaceWith("compositeLogRecordProcessor(processors)"))
public fun createCompositeLogRecordProcessor(processors: List<LogRecordProcessor>): LogRecordProcessor {
    return CompositeLogRecordProcessor(
        processors,
        NoopSdkErrorHandler
    )
}

/**
 * Creates a simple log record processor that immediately sends any telemetry to its exporter.
 */
@ExperimentalApi
@ConfigDsl
public fun LogExportConfigDsl.simpleLogRecordProcessor(exporter: LogRecordExporter): LogRecordProcessor {
    @Suppress("DEPRECATION")
    return createSimpleLogRecordProcessor(exporter)
}

@ExperimentalApi
@Deprecated("Deprecated.", ReplaceWith("simpleLogRecordProcessor(exporter)"))
public fun createSimpleLogRecordProcessor(exporter: LogRecordExporter): LogRecordProcessor {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    return SimpleLogRecordProcessor(exporter, scope)
}

/**
 * Creates a composite log record exporter that delegates to a list of exporters.
 */
@ExperimentalApi
@ConfigDsl
public fun LogExportConfigDsl.compositeLogRecordExporter(vararg exporters: LogRecordExporter): LogRecordExporter {
    require(exporters.isNotEmpty()) { "At least one exporter must be provided" }
    @Suppress("DEPRECATION")
    return createCompositeLogRecordExporter(exporters.toList())
}

@ExperimentalApi
@Deprecated("Deprecated.", ReplaceWith("compositeLogRecordExporter(exporters)"))
public fun createCompositeLogRecordExporter(exporters: List<LogRecordExporter>): LogRecordExporter {
    return CompositeLogRecordExporter(
        exporters,
        NoopSdkErrorHandler
    )
}

/**
 * Creates a batching processor that sends telemetry in batches.
 * See https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor
 */
@ExperimentalApi
@ConfigDsl
public fun LogExportConfigDsl.batchLogRecordProcessor(
    exporter: LogRecordExporter,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): LogRecordProcessor {
    @Suppress("DEPRECATION")
    return createBatchLogRecordProcessor(exporter, maxQueueSize, scheduleDelayMs, exportTimeoutMs, maxExportBatchSize, dispatcher)
}

@ExperimentalApi
@Deprecated(
    "Deprecated.",
    ReplaceWith("batchLogRecordProcessor(exporter, maxQueueSize, scheduleDelayMs, exportTimeoutMs, maxExportBatchSize, dispatcher)")
)
public fun createBatchLogRecordProcessor(
    exporter: LogRecordExporter,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): LogRecordProcessor =
    BatchLogRecordProcessorImpl(
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
@ConfigDsl
public fun LogExportConfigDsl.stdoutLogRecordExporter(
    logger: (String) -> Unit = ::println
): LogRecordExporter {
    @Suppress("DEPRECATION")
    return createStdoutLogRecordExporter(logger)
}

@Deprecated("Deprecated.", ReplaceWith("stdoutLogRecordExporter(logger)"))
@ExperimentalApi
public fun createStdoutLogRecordExporter(
    logger: (String) -> Unit = ::println
): LogRecordExporter = StdoutLogRecordExporter(logger)
