package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.propagation.TextMapPropagator

/**
 * Defines configuration for [io.opentelemetry.kotlin.OpenTelemetry].
 */
@ExperimentalApi
@ConfigDsl
public interface OpenTelemetryConfigDsl : ResourceConfigDsl, ConfigFileDsl {

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
     * Defines configuration for the [io.opentelemetry.kotlin.metrics.MeterProvider].
     */
    public fun meterProvider(action: MeterProviderConfigDsl.() -> Unit)

    /**
     * Defines configuration for detecting resource information from the environment. Detected
     * values are overridden by anything declared explicitly via [resource] or [serviceName].
     * https://opentelemetry.io/docs/specs/otel/resource/sdk/#detecting-resource-information-from-the-environment
     */
    public fun resourceDetection(action: ResourceDetectionConfigDsl.() -> Unit)

    /**
     * Defines configuration for how Context behaves.
     */
    public fun context(action: ContextConfigDsl.() -> Unit)

    /**
     * Configures the [TextMapPropagator] used to inject and extract context across process boundaries.
     * https://opentelemetry.io/docs/specs/otel/context/api-propagators/
     */
    public fun propagator(action: PropagatorConfigDsl.() -> TextMapPropagator)

    /**
     * Configures a custom [IdGenerator] that is used to generate trace and span IDs. If this is
     * not set the SDK will provide its own default implementation.
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#id-generators
     */
    public fun idGenerator(action: () -> IdGenerator)

    /**
     * Configures the [SdkErrorHandler] that is notified of errors and misuse detected by the SDK.
     * If this is not set the SDK discards these reports silently.
     * https://opentelemetry.io/docs/specs/otel/error-handling/#configuring-error-handlers
     */
    public fun errorHandler(handler: SdkErrorHandler)
}
