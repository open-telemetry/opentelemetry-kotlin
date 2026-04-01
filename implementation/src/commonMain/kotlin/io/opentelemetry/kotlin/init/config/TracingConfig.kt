package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler

/**
 * Configuration for the Tracing API.
 */
@ThreadSafe
internal class TracingConfig(

    /**
     * The processor to use for span data.
     */
    val processor: SpanProcessor?,

    /**
     * Limits on span data capture.
     */
    val spanLimits: SpanLimitConfig,

    /**
     * A resource to append to spans.
     */
    val resource: Resource,

    /**
     * Factory that produces the sampler to use when creating spans.
     */
    val samplerFactory: (SpanFactory) -> Sampler = { AlwaysOnSampler(it) },
)
