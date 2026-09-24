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
     * Console span exporter.
     */
    val console: ConsoleExporterBehavior? = null,
    /**
     * HTTP log exporter.
     */
    val http: OtlpHttpExporterBehavior? = null,
) : Behavior<SpanProcessorBehavior> {

    override fun mergeWith(higher: SpanProcessorBehavior): SpanProcessorBehavior = copy(
        console = mergeNode(console, higher.console),
        http = mergeNode(http, higher.http),
    )
}
