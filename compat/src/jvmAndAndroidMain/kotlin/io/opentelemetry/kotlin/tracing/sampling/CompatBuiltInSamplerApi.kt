package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAlwaysRecordSampler
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

private fun Sampler.toOtelJavaSampler(): OtelJavaSampler = when (this) {
    is SamplerAdapter -> impl
    else -> OtelJavaSamplerAdapter(this)
}
