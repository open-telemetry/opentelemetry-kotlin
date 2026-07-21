package io.opentelemetry.kotlin.init.config

import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.TracerConfigImpl
import io.opentelemetry.kotlin.tracing.TracerConfigurator
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOffSampler
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.ParentBasedSampler
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
     * Handler used to report errors and misuse of the SDK.
     */
    val sdkErrorHandler: SdkErrorHandler,

    /**
     * Factory that produces the sampler to use when creating spans.
     */
    val samplerFactory: (SpanFactory) -> Sampler = { _ ->
        ParentBasedSampler(
            root = AlwaysOnSampler(),
            remoteParentSampled = AlwaysOnSampler(),
            remoteParentNotSampled = AlwaysOffSampler(),
            localParentSampled = AlwaysOnSampler(),
            localParentNotSampled = AlwaysOffSampler(),
        )
    },

    /**
     * Computes the per-tracer config.
     */
    val tracerConfigurator: TracerConfigurator = TracerConfigurator { TracerConfigImpl(true) },
)
