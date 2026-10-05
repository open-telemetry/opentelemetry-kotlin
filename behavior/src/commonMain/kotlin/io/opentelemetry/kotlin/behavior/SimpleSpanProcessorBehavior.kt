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
class SimpleSpanProcessorBehavior : Behavior<SimpleSpanProcessorBehavior> {

    override fun mergeWith(higher: SimpleSpanProcessorBehavior): SimpleSpanProcessorBehavior = higher

    override fun equals(other: Any?): Boolean = other is SimpleSpanProcessorBehavior

    override fun hashCode(): Int = 0
}
