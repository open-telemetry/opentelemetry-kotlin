package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Processor used by the tracer provider.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#spanprocessor
 */
@ExperimentalApi
class SpanProcessorBehavior : Behavior<SpanProcessorBehavior> {

    override fun mergeWith(higher: SpanProcessorBehavior): SpanProcessorBehavior = higher

    override fun equals(other: Any?): Boolean = other is SpanProcessorBehavior

    override fun hashCode(): Int = 0
}
