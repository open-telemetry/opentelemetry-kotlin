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
     * Simple processor, which exports each span as it ends.
     */
    val simple: SimpleSpanProcessorBehavior? = null,
    /** Batching settings for the configured exporter. */
    val batch: BatchSpanProcessorBehavior? = null,
) : Behavior<SpanProcessorBehavior> {
    override fun mergeWith(higher: SpanProcessorBehavior): SpanProcessorBehavior = copy(
        simple = mergeNode(simple, higher.simple),
        batch = mergeNode(batch, higher.batch),
    )
}
