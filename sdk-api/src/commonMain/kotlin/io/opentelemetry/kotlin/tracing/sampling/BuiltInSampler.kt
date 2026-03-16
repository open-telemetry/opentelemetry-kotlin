package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Built-in sampling strategies that can be configured during SDK initialization.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#built-in-samplers
 */
@ExperimentalApi
public enum class BuiltInSampler {
    ALWAYS_ON,
}
