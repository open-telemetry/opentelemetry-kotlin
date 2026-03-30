package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.Sampler

/**
 * Defines configuration for the [io.opentelemetry.kotlin.tracing.TracerProvider].
 */
@ExperimentalApi
@ConfigDsl
public interface TracerProviderConfigDsl : ResourceConfigDsl {

    /**
     * The span limits configuration for this tracer provider. Processors will be invoked
     * in the order in which they were added.
     */
    public fun spanLimits(action: SpanLimitsConfigDsl.() -> Unit)

    /**
     * Configures how spans should be processed and exported.
     */
    public fun export(action: TraceExportConfigDsl.() -> SpanProcessor)

    /**
     * Configures the strategy that should be used for sampling.
     */
    public fun sampler(action: SamplerConfigDsl.() -> Sampler)
}
