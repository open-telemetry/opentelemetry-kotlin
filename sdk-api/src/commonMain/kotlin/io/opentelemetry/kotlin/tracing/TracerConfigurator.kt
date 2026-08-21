package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo

/**
 * Dynamically controls how a [Tracer] should behave by constructing a [TracerConfig].
 * Implementations must return quickly as this code may be invoked frequently.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#tracerconfigurator
 */
@ExperimentalApi
public fun interface TracerConfigurator {

    /**
     * Returns the [TracerConfig] for the given [scope].
     */
    public fun tracerConfig(scope: InstrumentationScopeInfo): TracerConfig
}
