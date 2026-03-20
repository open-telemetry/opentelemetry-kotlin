package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines configuration for [io.opentelemetry.kotlin.OpenTelemetry].
 */
@ExperimentalApi
@ConfigDsl
public interface OpenTelemetryConfigDsl : ResourceConfigDsl {

    /**
     * Defines global attribute limits. This can be overridden on individual signals.
     * https://opentelemetry.io/docs/specs/otel/common/#attribute-limits
     */
    public fun attributeLimits(action: AttributeLimitsConfigDsl.() -> Unit)

    /**
     * Defines configuration for the [io.opentelemetry.kotlin.tracing.TracerProvider].
     */
    public fun tracerProvider(action: TracerProviderConfigDsl.() -> Unit)

    /**
     * Defines configuration for the [io.opentelemetry.kotlin.logging.LoggerProvider].
     */
    public fun loggerProvider(action: LoggerProviderConfigDsl.() -> Unit)

    /**
     * Defines configuration for how Context behaves.
     */
    public fun context(action: ContextConfigDsl.() -> Unit)
}
