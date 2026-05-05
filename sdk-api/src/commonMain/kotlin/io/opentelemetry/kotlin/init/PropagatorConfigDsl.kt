package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.TextMapPropagator

/**
 * Configures the [TextMapPropagator] used to inject and extract context across process boundaries.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/
 */
@ExperimentalApi
@ConfigDsl
public interface PropagatorConfigDsl {

    /**
     * Returns a [TextMapPropagator] that sequentially delegates to each of [propagators].
     *
     * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#composite-propagator
     */
    public fun composite(vararg propagators: TextMapPropagator): TextMapPropagator

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
