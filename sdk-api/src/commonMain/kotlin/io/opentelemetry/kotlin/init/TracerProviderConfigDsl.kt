package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.export.SpanProcessor

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
}
