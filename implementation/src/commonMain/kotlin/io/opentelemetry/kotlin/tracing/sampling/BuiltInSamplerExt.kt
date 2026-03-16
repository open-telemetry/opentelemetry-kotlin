package io.opentelemetry.kotlin.tracing.sampling

internal fun BuiltInSampler.toSampler(): Sampler = when (this) {
    BuiltInSampler.ALWAYS_ON -> AlwaysOnSampler
}
