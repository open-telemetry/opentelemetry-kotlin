package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.propagation.TextMapPropagator

internal class OpenTelemetryConfigImpl(
    clock: Clock,
    sdkErrorHandler: SdkErrorHandler,
    private val globalResourceConfig: ResourceConfigImpl = ResourceConfigImpl(),
) : OpenTelemetryConfigDsl, ResourceConfigDsl by globalResourceConfig {

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

    internal fun resolveIdGenerator(): IdGenerator = customIdGenerator?.invoke() ?: IdGeneratorImpl()

    private val defaultResource by lazy(::sdkDefaultResource)

    internal fun generateTracingConfig() =
        tracingConfig.generateTracingConfig(defaultResource.merge(globalResourceConfig.generateResource()), globalAttributeLimits)

    internal fun generateLoggingConfig() =
        loggingConfig.generateLoggingConfig(defaultResource.merge(globalResourceConfig.generateResource()), globalAttributeLimits)

    internal fun generateMetricsConfig() =
        metricsConfig.generateMetricsConfig(defaultResource.merge(globalResourceConfig.generateResource()))
}
