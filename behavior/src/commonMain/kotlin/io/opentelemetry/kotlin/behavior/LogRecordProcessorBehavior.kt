package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Processor used by the logger provider.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordprocessor
 */
@ExperimentalApi
data class LogRecordProcessorBehavior(

    /**
     * Log exporter configuration.
     */
    val exporter: LogExporterBehavior? = null,

) : Behavior<LogRecordProcessorBehavior> {

    override fun mergeWith(higher: LogRecordProcessorBehavior): LogRecordProcessorBehavior = copy(
        exporter = mergeNode(exporter, higher.exporter),
    )
}
