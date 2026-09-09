package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Behavior of tracing.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#tracer-provider
 */
@ExperimentalApi
data class TracerProviderBehavior(

    /**
     * Limits on span data capture.
     */
    val spanLimits: SpanLimitsBehavior? = null,

    /**
     * Processor used by the tracer provider.
     */
    val processor: SpanProcessorBehavior? = null,

    /**
     * Sampler the tracer provider should use.
     */
    val sampler: SamplerBehavior? = null,

) : Behavior<TracerProviderBehavior> {

    override fun mergeWith(higher: TracerProviderBehavior): TracerProviderBehavior = copy(
        spanLimits = mergeNode(spanLimits, higher.spanLimits),
        processor = mergeNode(processor, higher.processor),
        sampler = mergeNode(sampler, higher.sampler)
    )
}
