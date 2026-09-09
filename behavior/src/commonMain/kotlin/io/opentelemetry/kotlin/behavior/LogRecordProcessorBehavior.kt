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
     * Console log exporter. Selecting it is the whole configuration.
     */
    val console: ConsoleExporterBehavior? = null,

) : Behavior<LogRecordProcessorBehavior> {

    override fun mergeWith(higher: LogRecordProcessorBehavior): LogRecordProcessorBehavior = copy(
        console = mergeNode(console, higher.console),
    )
}
