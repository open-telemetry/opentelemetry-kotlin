package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Processor used by the tracer provider.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#spanprocessor
 */
@ExperimentalApi
data class SpanProcessorBehavior(

    /**
     * Console span exporter. Selecting it is the whole configuration.
     */
    val console: ConsoleExporterBehavior? = null,

) : Behavior<SpanProcessorBehavior> {

    override fun mergeWith(higher: SpanProcessorBehavior): SpanProcessorBehavior = copy(
        console = mergeNode(console, higher.console),
    )
}
