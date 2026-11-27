package io.opentelemetry.kotlin.behavior.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.mergeList
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Behavior of tracing.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#tracer-provider
 */
@OptIn(ExperimentalApi::class)
data class TracerProviderBehavior(

    /**
     * Limits on span data capture.
     */
    val spanLimits: SpanLimitsBehavior? = null,

    /**
     * Processor used by the tracer provider.
     */
    val processors: List<SpanProcessorBehavior> = emptyList(),

    /**
     * Sampler the tracer provider should use.
     */
    val sampler: SamplerBehavior? = null,

    /**
     * Strategy used to generate trace and span IDs.
     */
    val idGenerator: IdGeneratorBehavior? = null,

) : Behavior<TracerProviderBehavior> {

    override fun mergeWith(higher: TracerProviderBehavior): TracerProviderBehavior = copy(
        spanLimits = mergeNode(spanLimits, higher.spanLimits),
        processors = mergeList(processors, higher.processors),
        sampler = mergeNode(sampler, higher.sampler),
        idGenerator = mergeNode(idGenerator, higher.idGenerator),
    )
}
