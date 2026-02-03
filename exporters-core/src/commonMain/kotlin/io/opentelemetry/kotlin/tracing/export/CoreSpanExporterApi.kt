package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Creates a composite span processor that delegates to a list of processors.
 */
@ExperimentalApi
public fun createCompositeSpanProcessor(processors: List<SpanProcessor>): SpanProcessor {
    return CompositeSpanProcessor(
        processors,
        NoopSdkErrorHandler
    )
}

/**
 * Creates a simple span processor that immediately sends any telemetry to its exporter.
 */
@ExperimentalApi
public fun createSimpleSpanProcessor(exporter: SpanExporter): SpanProcessor {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    return SimpleSpanProcessor(exporter, scope)
}

/**
 * Creates a composite span exporter that delegates to a list of exporters.
 */
@ExperimentalApi
public fun createCompositeSpanExporter(exporters: List<SpanExporter>): SpanExporter {
    return CompositeSpanExporter(
        exporters,
        NoopSdkErrorHandler
    )
}

/**
 * Creates a batching processor that sends telemetry in batches.
 * See https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor
 */
@OptIn(ExperimentalApi::class)
public fun createBatchSpanProcessor(
    exporter: SpanExporter,
    maxQueueSize: Int = BatchTelemetryDefaults.MAX_QUEUE_SIZE,
    scheduleDelayMs: Long = BatchTelemetryDefaults.SCHEDULE_DELAY_MS,
    exportTimeoutMs: Long = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
    maxExportBatchSize: Int = BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE
): SpanProcessor = BatchSpanProcessorImpl(
    exporter,
    maxQueueSize,
    scheduleDelayMs,
    exportTimeoutMs,
    maxExportBatchSize
)

/**
 * Creates a span exporter that outputs span data to stdout. The destination is configurable
 * via a parameter that defaults to [println].
 *
 * This exporter is intended for debugging and learning purposes. It is not recommended for
 * production use. The output format is not standardized and can change at any time.
 */
@ExperimentalApi
public fun createStdoutSpanExporter(
    logger: (String) -> Unit = ::println
): SpanExporter = StdoutSpanExporter(logger)
