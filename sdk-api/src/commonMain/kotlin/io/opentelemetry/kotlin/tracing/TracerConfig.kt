package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Immutable model of how a [Tracer] should behave.
 */
@ExperimentalApi
public interface TracerConfig {

    /**
     * Whether the tracer is enabled.
     */
    public val enabled: Boolean
}
