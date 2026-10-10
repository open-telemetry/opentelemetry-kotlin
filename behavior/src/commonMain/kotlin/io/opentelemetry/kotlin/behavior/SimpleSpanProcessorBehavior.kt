package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Unbuffered processing for the tracer provider: each span is exported as it ends. The exporter is
 * configured on [SpanProcessorBehavior]. This type has no fields; selecting it is the whole
 * configuration.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#simple-processor
 */
@ExperimentalApi
data class SimpleSpanProcessorBehavior(
    /** Span exporter. */
    val exporter: SpanExporterBehavior? = null
) : Behavior<SimpleSpanProcessorBehavior> {
    override fun mergeWith(higher: SimpleSpanProcessorBehavior): SimpleSpanProcessorBehavior = copy(
        exporter = mergeNode(exporter, higher.exporter)
    )
}
