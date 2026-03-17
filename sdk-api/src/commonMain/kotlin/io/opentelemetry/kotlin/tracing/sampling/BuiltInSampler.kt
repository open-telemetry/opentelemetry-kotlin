package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Built-in sampling strategies that can be configured during SDK initialization.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#built-in-samplers
 */
@ExperimentalApi
public enum class BuiltInSampler {

    /**
     * Spans will always be recorded and sampled.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwayson
     */
    ALWAYS_ON,

    /**
     * Spans will never be recorded and sampled.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysoff
     */
    ALWAYS_OFF,
}
