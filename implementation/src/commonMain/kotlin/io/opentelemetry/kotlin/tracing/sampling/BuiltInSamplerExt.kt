package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.factory.SpanFactory

internal fun BuiltInSampler.toSampler(spanFactory: SpanFactory): Sampler = when (this) {
    BuiltInSampler.ALWAYS_ON -> AlwaysOnSampler(spanFactory)
}
