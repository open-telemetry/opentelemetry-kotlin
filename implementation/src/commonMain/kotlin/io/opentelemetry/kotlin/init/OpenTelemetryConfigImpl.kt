package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import kotlin.concurrent.Volatile

internal class OpenTelemetryConfigImpl(
    clock: Clock,
    private val globalResourceConfig: ResourceConfigImpl = ResourceConfigImpl(),
) : OpenTelemetryConfigDsl, ResourceConfigDsl by globalResourceConfig {

    @Volatile private var configuredErrorHandler: SdkErrorHandler = NoopSdkErrorHandler

    /**
     * The handler is configured after the sub-configs below have been created, so they receive a
     * forwarder that resolves the configured handler on each report instead.
     */
    private val sdkErrorHandler = GuardedSdkErrorHandler { configuredErrorHandler.onError(it) }

    internal val tracingConfig: TracerProviderConfigImpl = TracerProviderConfigImpl(clock, sdkErrorHandler)
    internal val loggingConfig: LoggerProviderConfigImpl = LoggerProviderConfigImpl(clock, sdkErrorHandler)
    internal val metricsConfig: MeterProviderConfigImpl = MeterProviderConfigImpl(sdkErrorHandler)
    internal val contextConfig: ContextConfigImpl = ContextConfigImpl()
    internal val propagatorCfg: PropagatorConfigImpl = PropagatorConfigImpl()
    private val globalAttributeLimits = AttributeLimitsConfigImpl()

    private var customIdGenerator: (() -> IdGenerator)? = null

    override fun attributeLimits(action: AttributeLimitsConfigDsl.() -> Unit) {
        globalAttributeLimits.action()
    }

    override fun tracerProvider(action: TracerProviderConfigDsl.() -> Unit) {
        tracingConfig.action()
    }

    override fun loggerProvider(action: LoggerProviderConfigDsl.() -> Unit) {
        loggingConfig.action()
    }

    override fun meterProvider(action: MeterProviderConfigDsl.() -> Unit) {
        metricsConfig.action()
    }

    override fun context(action: ContextConfigDsl.() -> Unit) {
        contextConfig.action()
    }

    override fun propagator(action: PropagatorConfigDsl.() -> TextMapPropagator) {
        propagatorCfg.action()
    }

    override fun idGenerator(action: () -> IdGenerator) {
        customIdGenerator = action
    }

    override fun errorHandler(handler: SdkErrorHandler) {
        configuredErrorHandler = handler
    }

    internal fun resolveIdGenerator(): IdGenerator = customIdGenerator?.invoke() ?: IdGeneratorImpl()

    private val defaultResource by lazy(::sdkDefaultResource)

    internal fun generateTracingConfig() =
        tracingConfig.generateTracingConfig(defaultResource.merge(globalResourceConfig.generateResource()), globalAttributeLimits)

    internal fun generateLoggingConfig() =
        loggingConfig.generateLoggingConfig(defaultResource.merge(globalResourceConfig.generateResource()), globalAttributeLimits)

    internal fun generateMetricsConfig() =
        metricsConfig.generateMetricsConfig(defaultResource.merge(globalResourceConfig.generateResource()))
}
