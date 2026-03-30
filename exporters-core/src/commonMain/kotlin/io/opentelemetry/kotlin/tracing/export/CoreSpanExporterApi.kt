package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates a composite span processor that delegates to a list of processors.
 */
@ExperimentalApi
public fun TraceExportConfigDsl.compositeSpanProcessor(vararg processors: SpanProcessor): SpanProcessor {
    require(processors.isNotEmpty()) { "At least one processor must be provided" }
    return CompositeSpanProcessor(processors.toList(), NoopSdkErrorHandler)
}

/**
 * Creates a simple span processor that immediately sends any telemetry to its exporter.
 */
@ExperimentalApi
public fun TraceExportConfigDsl.simpleSpanProcessor(exporter: SpanExporter): SpanProcessor {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    return SimpleSpanProcessor(exporter, scope)
}

/**
 * Creates a composite span exporter that delegates to a list of exporters.
 */
@ExperimentalApi
public fun TraceExportConfigDsl.compositeSpanExporter(vararg exporters: SpanExporter): SpanExporter {
    require(exporters.isNotEmpty()) { "At least one exporter must be provided" }
    return CompositeSpanExporter(exporters.toList(), NoopSdkErrorHandler)
}

/**
 * Creates a batching processor that sends telemetry in batches.
 * See https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor
 */
@ExperimentalApi
public fun TraceExportConfigDsl.batchSpanProcessor(
    exporter: SpanExporter,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
): SpanProcessor = BatchSpanProcessorImpl(
    exporter,
    maxQueueSize,
    scheduleDelayMs,
    exportTimeoutMs,
    maxExportBatchSize,
    dispatcher,
)

/**
 * Creates a span exporter that outputs span data to stdout. The destination is configurable
 * via a parameter that defaults to [println].
 *
 * This exporter is intended for debugging and learning purposes. It is not recommended for
 * production use. The output format is not standardized and can change at any time.
 */
@ExperimentalApi
public fun TraceExportConfigDsl.stdoutSpanExporter(
    logger: (String) -> Unit = ::println
): SpanExporter = StdoutSpanExporter(logger)
