package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.init.SamplerConfigDsl

/**
 * Configures sampling so that spans are always recorded and sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwayson
 */
@ExperimentalApi
public fun SamplerConfigDsl.alwaysOn(): Sampler = AlwaysOnSampler()

/**
 * Configures sampling so that spans are never recorded and sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysoff
 */
@ExperimentalApi
public fun SamplerConfigDsl.alwaysOff(): Sampler = AlwaysOffSampler()

/**
 * Configures sampling based on the parent span's sampling decision.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#parentbased
 */
@ExperimentalApi
public fun SamplerConfigDsl.parentBased(
    root: Sampler,
    remoteParentSampled: Sampler = alwaysOn(),
    remoteParentNotSampled: Sampler = alwaysOff(),
    localParentSampled: Sampler = alwaysOn(),
    localParentNotSampled: Sampler = alwaysOff(),
): Sampler = ParentBasedSampler(
    root = root,
    remoteParentSampled = remoteParentSampled,
    remoteParentNotSampled = remoteParentNotSampled,
    localParentSampled = localParentSampled,
    localParentNotSampled = localParentNotSampled,
)
