package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Unbuffered processing for the logger provider's processor: each log record is exported as it is
 * emitted.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#simple-processor
 */
@ExperimentalApi
data class SimpleLogRecordProcessorBehavior(

    /**
     * Exporter that receives each log record.
     * */
    val exporter: LogRecordExporterBehavior? = null,

) : Behavior<SimpleLogRecordProcessorBehavior> {

    override fun mergeWith(higher: SimpleLogRecordProcessorBehavior): SimpleLogRecordProcessorBehavior = copy(
        exporter = mergeNode(exporter, higher.exporter),
    )
}
