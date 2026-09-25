package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.BaggageFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory

/**
 * Constructs Propagator instances. This does not require an
 * [io.opentelemetry.kotlin.OpenTelemetry] instance, so propagators can be obtained before (or
 * without) the SDK being initialized. This can be useful for instrumentation authors who want to
 * propagate even when an SDK isn't active.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/
 */
@ExperimentalApi
public interface Propagators : PropagatorFactory {

    /**
     * Returns a [TextMapPropagator] that injects and extracts nothing.
     *
     * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#textmap-propagator
     */
    public fun none(): TextMapPropagator

    /**
     * Factory that constructs SpanContext objects.
     */
    public val spanContext: SpanContextFactory

    /**
     * Factory that constructs TraceFlags objects.
     */
    public val traceFlags: TraceFlagsFactory

    /**
     * Factory that constructs TraceState objects.
     */
    public val traceState: TraceStateFactory

    /**
     * Factory that constructs Baggage objects.
     */
    public val baggage: BaggageFactory
}
