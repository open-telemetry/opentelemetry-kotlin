package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Creates [TextMapPropagator] instances.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/
 */
@ExperimentalApi
public interface PropagatorFactory {

    /**
     * Returns a [TextMapPropagator] that sequentially delegates to each of [propagators].
     *
     * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#composite-propagator
     */
    public fun composite(vararg propagators: TextMapPropagator): TextMapPropagator

    /**
     * Returns a [TextMapPropagator] that sequentially delegates to each of [propagators].
     *
     * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#composite-propagator
     */
    public fun composite(propagators: List<TextMapPropagator>): TextMapPropagator
}
