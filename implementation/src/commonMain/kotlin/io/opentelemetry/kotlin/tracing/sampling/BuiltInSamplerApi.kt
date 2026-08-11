package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
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
 * Configures sampling so that spans are always recorded, even if the delegate sampler
 * would otherwise drop them.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysrecord
 */
@ExperimentalApi
public fun SamplerConfigDsl.alwaysRecord(root: Sampler): Sampler = AlwaysRecordSampler(root)

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

/**
 * Configures sampling by delegating to a [ComposableSampler], using consistent probability
 * sampling over the OpenTelemetry TraceState `ot` `th`/`rv` sub-keys.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#compositesampler
 */
@ExperimentalApi
public fun SamplerConfigDsl.composite(block: SamplerConfigDsl.() -> ComposableSampler): Sampler =
    CompositeSampler(block())

/**
 * A [ComposableSampler] that always samples, regardless of parent trace state.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwayson
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableAlwaysOn(): ComposableSampler = ComposableAlwaysOnSampler()

/**
 * A [ComposableSampler] that never samples.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwaysoff
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableAlwaysOff(): ComposableSampler = ComposableAlwaysOffSampler()

/**
 * A [ComposableSampler] that samples spans with the given probability [ratio].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableprobability
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableProbability(ratio: Double): ComposableSampler =
    if (ratio == 0.0) {
        ComposableAlwaysOffSampler()
    } else {
        ComposableProbabilitySampler(ratio)
    }

/**
 * A [ComposableSampler] that honors the parent's sampling threshold when present, falling back
 * to [root] when there is no valid parent.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableparentthreshold
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableParentThreshold(root: ComposableSampler): ComposableSampler =
    ComposableParentThresholdSampler(root)

/**
 * A [ComposableSampler] that leaves the sampling decision to [delegate] but adds attributes to
 * spans that are sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableannotating
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableAnnotating(
    delegate: ComposableSampler,
    attributes: AttributesMutator.() -> Unit,
): ComposableSampler = ComposableAnnotatingSampler(delegate, attributes)
