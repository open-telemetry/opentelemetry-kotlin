package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.dsl.AttributeLimitsConfigDslImpl
import io.opentelemetry.kotlin.config.dsl.BehaviorSupplier
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import kotlin.concurrent.Volatile

internal class OpenTelemetryConfigImpl(
    clock: Clock,
    internal val globalResourceConfig: ResourceConfigImpl = ResourceConfigImpl(),
) : OpenTelemetryConfigDsl,
    ResourceConfigDsl by globalResourceConfig,
    BehaviorSupplier<OpenTelemetryBehavior> {

    @Volatile private var configuredErrorHandler: SdkErrorHandler = NoopSdkErrorHandler

    /**
     * The handler is configured after the sub-configs below have been created, so they receive a
     * forwarder that resolves the configured handler on each report instead.
     */
    internal val sdkErrorHandler = GuardedSdkErrorHandler { configuredErrorHandler.onError(it) }

    internal val tracingConfig: TracerProviderConfigImpl = TracerProviderConfigImpl(clock, sdkErrorHandler)
    internal val loggingConfig: LoggerProviderConfigImpl = LoggerProviderConfigImpl(clock, sdkErrorHandler)
    internal val metricsConfig: MeterProviderConfigImpl = MeterProviderConfigImpl(sdkErrorHandler)
    internal val contextConfig: ContextConfigImpl = ContextConfigImpl()
    internal val propagatorCfg: PropagatorConfigImpl = PropagatorConfigImpl()
    private val globalAttributeLimits = AttributeLimitsConfigDslImpl()
    internal val resourceDetectionConfig = ResourceDetectionConfigImpl()

    @Volatile internal var customIdGenerator: (() -> IdGenerator)? = null
        private set

    @Volatile internal var configFilePath: String? = null
        private set

    override fun configFile(path: String) {
        configFilePath = path
    }

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

    override fun resourceDetection(action: ResourceDetectionConfigDsl.() -> Unit) {
        resourceDetectionConfig.action()
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

    override fun toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
        attributeLimits = globalAttributeLimits.toBehavior(),
        tracerProvider = tracingConfig.toBehavior(),
    )
}
