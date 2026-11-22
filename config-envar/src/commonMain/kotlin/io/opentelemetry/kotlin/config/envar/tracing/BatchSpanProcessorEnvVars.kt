package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorSupplier
import io.opentelemetry.kotlin.behavior.tracing.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/** Reads the standard batch span processor environment variables. */
@OptIn(ExperimentalApi::class)
internal class BatchSpanProcessorEnvVars(
    private val reader: ReportingEnvVarReader
) : BehaviorSupplier<BatchSpanProcessorBehavior> {
    override fun toBehavior(): BatchSpanProcessorBehavior = BatchSpanProcessorBehavior(
        scheduleDelay = reader.readNonNegativeLong(SCHEDULE_DELAY),
        exportTimeout = reader.readNonNegativeLong(EXPORT_TIMEOUT),
        maxQueueSize = reader.readNonNegativeInt(MAX_QUEUE_SIZE, acceptZero = false),
        maxExportBatchSize = reader.readNonNegativeInt(MAX_EXPORT_BATCH_SIZE, acceptZero = false),
        exporter = SpanExporterEnvVars(reader).toBehavior(),
    )

    internal companion object {
        const val SCHEDULE_DELAY = "OTEL_BSP_SCHEDULE_DELAY"
        const val EXPORT_TIMEOUT = "OTEL_BSP_EXPORT_TIMEOUT"
        const val MAX_QUEUE_SIZE = "OTEL_BSP_MAX_QUEUE_SIZE"
        const val MAX_EXPORT_BATCH_SIZE = "OTEL_BSP_MAX_EXPORT_BATCH_SIZE"
    }
}
