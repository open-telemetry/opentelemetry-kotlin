package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.init.ConfigDsl
import io.opentelemetry.kotlin.init.SamplerConfigDsl

/**
 * Configures sampling so that spans are always recorded and sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwayson
 */
@ExperimentalApi
@ConfigDsl
public fun SamplerConfigDsl.alwaysOn(): Sampler = SamplerAdapter(OtelJavaSampler.alwaysOn())

/**
 * Configures sampling so that spans are never recorded and sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysoff
 */
@ExperimentalApi
@ConfigDsl
public fun SamplerConfigDsl.alwaysOff(): Sampler = SamplerAdapter(OtelJavaSampler.alwaysOff())
