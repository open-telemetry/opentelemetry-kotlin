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

    /**
     * Returns a [TextMapPropagator] that injects and extracts [io.opentelemetry.kotlin.baggage.Baggage]
     * via the W3C `baggage` HTTP header.
     *
     * https://www.w3.org/TR/baggage/
     */
    public fun w3cBaggage(): TextMapPropagator

    /**
     * Returns a [TextMapPropagator] that injects and extracts the current span context
     * via the W3C `traceparent` and `tracestate` HTTP headers.
     *
     * https://www.w3.org/TR/trace-context/
     */
    public fun w3cTraceContext(): TextMapPropagator
}
