package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAlwaysRecordSampler
import io.opentelemetry.kotlin.aliases.OtelJavaComposableSampler
import io.opentelemetry.kotlin.aliases.OtelJavaCompositeSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.init.SamplerConfigDsl

/**
 * Configures sampling so that spans are always recorded and sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwayson
 */
@ExperimentalApi
public fun SamplerConfigDsl.alwaysOn(): Sampler = SamplerAdapter(OtelJavaSampler.alwaysOn())

/**
 * Configures sampling so that spans are never recorded and sampled.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysoff
 */
@ExperimentalApi
public fun SamplerConfigDsl.alwaysOff(): Sampler = SamplerAdapter(OtelJavaSampler.alwaysOff())

/**
 * Configures sampling so that spans are always recorded, even if the delegate sampler
 * would otherwise drop them.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysrecord
 */
@ExperimentalApi
public fun SamplerConfigDsl.alwaysRecord(root: Sampler): Sampler =
    SamplerAdapter(OtelJavaAlwaysRecordSampler.create(root.toOtelJavaSampler()))

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
): Sampler = SamplerAdapter(
    OtelJavaSampler.parentBasedBuilder(root.toOtelJavaSampler())
        .setRemoteParentSampled(remoteParentSampled.toOtelJavaSampler())
        .setRemoteParentNotSampled(remoteParentNotSampled.toOtelJavaSampler())
        .setLocalParentSampled(localParentSampled.toOtelJavaSampler())
        .setLocalParentNotSampled(localParentNotSampled.toOtelJavaSampler())
        .build()
)

/**
 * Configures sampling by delegating to a [ComposableSampler], using consistent probability
 * sampling over the OpenTelemetry TraceState `ot` `th`/`rv` sub-keys.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#compositesampler
 */
@ExperimentalApi
public fun SamplerConfigDsl.composite(block: SamplerConfigDsl.() -> ComposableSampler): Sampler =
    SamplerAdapter(OtelJavaCompositeSampler.wrap(block().toOtelJavaComposableSampler()))

/**
 * A [ComposableSampler] that always samples, regardless of parent trace state.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwayson
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableAlwaysOn(): ComposableSampler =
    OtelJavaBackedComposableSampler(OtelJavaComposableSampler.alwaysOn())

/**
 * A [ComposableSampler] that never samples.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwaysoff
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableAlwaysOff(): ComposableSampler =
    OtelJavaBackedComposableSampler(OtelJavaComposableSampler.alwaysOff())

/**
 * A [ComposableSampler] that samples spans with the given probability [ratio].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableprobability
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableProbability(ratio: Double): ComposableSampler =
    OtelJavaBackedComposableSampler(OtelJavaComposableSampler.probability(ratio))

/**
 * A [ComposableSampler] that honors the parent's sampling threshold when present, falling back
 * to [root] when there is no valid parent.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableparentthreshold
 */
@ExperimentalApi
public fun SamplerConfigDsl.composableParentThreshold(root: ComposableSampler): ComposableSampler =
    OtelJavaBackedComposableSampler(OtelJavaComposableSampler.parentThreshold(root.toOtelJavaComposableSampler()))

private fun Sampler.toOtelJavaSampler(): OtelJavaSampler = when (this) {
    is SamplerAdapter -> impl
    else -> OtelJavaSamplerAdapter(this)
}

private fun ComposableSampler.toOtelJavaComposableSampler(): OtelJavaComposableSampler = when (this) {
    is OtelJavaBackedComposableSampler -> impl
    else -> KotlinComposableSamplerAdapter(this)
}
