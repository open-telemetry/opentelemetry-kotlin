package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.BehaviorSupplier
import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.envar.reader.ReportingEnvVarReader

/**
 * Reads the standard batch log record processor environment variables.
 *
 * https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#batch-logrecord-processor
 */
@OptIn(ExperimentalApi::class)
internal class BatchLogRecordProcessorEnvVars(
    private val reader: ReportingEnvVarReader
) : BehaviorSupplier<BatchLogRecordProcessorBehavior> {
    override fun toBehavior(): BatchLogRecordProcessorBehavior = BatchLogRecordProcessorBehavior(
        scheduleDelay = reader.readNonNegativeLong(SCHEDULE_DELAY),
        exportTimeout = reader.readNonNegativeLong(EXPORT_TIMEOUT),
        maxQueueSize = reader.readNonNegativeInt(MAX_QUEUE_SIZE, acceptZero = false),
        maxExportBatchSize = reader.readNonNegativeInt(MAX_EXPORT_BATCH_SIZE, acceptZero = false),
        exporter = LogRecordExporterEnvVars(reader).toBehavior(),
    )

    internal companion object {
        const val SCHEDULE_DELAY = "OTEL_BLRP_SCHEDULE_DELAY"
        const val EXPORT_TIMEOUT = "OTEL_BLRP_EXPORT_TIMEOUT"
        const val MAX_QUEUE_SIZE = "OTEL_BLRP_MAX_QUEUE_SIZE"
        const val MAX_EXPORT_BATCH_SIZE = "OTEL_BLRP_MAX_EXPORT_BATCH_SIZE"
    }
}
