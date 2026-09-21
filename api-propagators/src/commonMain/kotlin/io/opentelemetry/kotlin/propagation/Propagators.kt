package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Constructs Propagator instances. This does not require an
 * [io.opentelemetry.kotlin.OpenTelemetry] instance, so propagators can be obtained before (or
 * without) the SDK being initialized. This can be useful for instrumentation authors who want to
 * propagate even when an SDK isn't active.
 *
 * https://opentelemetry.io/docs/specs/otel/context/api-propagators/
 */
@ExperimentalApi
public interface Propagators {

    /**
     * Returns a [TextMapPropagator] that injects and extracts nothing.
     *
     * https://opentelemetry.io/docs/specs/otel/context/api-propagators/#textmap-propagator
     */
    public fun none(): TextMapPropagator
}
