package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Processor used by the tracer provider.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#spanprocessor
 */
@ExperimentalApi
sealed class SpanProcessorBehavior : Behavior<SpanProcessorBehavior> {

    abstract val exporter: SpanExporterBehavior?

    /**
     * Simple span processor.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#simpleprocessor
     */
    data class Simple(
        override val exporter: SpanExporterBehavior? = null,
    ) : SpanProcessorBehavior() {
        override fun mergeWith(higher: SpanProcessorBehavior): SpanProcessorBehavior {
            if (higher !is Simple) {
                return higher
            }
            return copy(exporter = mergeNode(exporter, higher.exporter))
        }
    }

    /**
     * Batch span processor.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#batchprocessor
     */
    data class Batch(
        override val exporter: SpanExporterBehavior? = null,
    ) : SpanProcessorBehavior() {
        override fun mergeWith(higher: SpanProcessorBehavior): SpanProcessorBehavior {
            if (higher !is Batch) {
                return higher
            }
            return copy(exporter = mergeNode(exporter, higher.exporter))
        }
    }
}
