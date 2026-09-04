package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.dsl.AttributeLimitsConfigDslImpl
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.resource.detectResource
import kotlin.concurrent.Volatile

internal class OpenTelemetryConfigImpl(
    clock: Clock,
    private val resourceFactory: ResourceFactory = ResourceFactoryImpl(),
    private val globalResourceConfig: ResourceConfigImpl = ResourceConfigImpl(),
    private val behaviorReader: BehaviorReader = defaultBehaviorReader(),
) : OpenTelemetryConfigDsl, ResourceConfigDsl by globalResourceConfig {

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
    private val resourceDetectionConfig = ResourceDetectionConfigImpl()

    private var customIdGenerator: (() -> IdGenerator)? = null

    @Volatile private var configFilePath: String? = null

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

    internal fun resolveIdGenerator(): IdGenerator = customIdGenerator?.invoke() ?: IdGeneratorImpl()

    private val baseResource by lazy {
        sdkDefaultResource()
            .merge(resourceDetectionConfig.detectors.detectResource(resourceFactory, sdkErrorHandler))
            .merge(globalResourceConfig.generateResource())
    }

    /**
     * The behavior the SDK is initialized with, applying the precedence rules the resolver defines.
     */
    private val resolvedBehavior: OpenTelemetryBehavior by lazy {
        behaviorReader.read(
            configFilePath = configFilePath,
            dsl = OpenTelemetryBehavior(attributeLimits = globalAttributeLimits.toBehavior()),
        )
    }

    private fun resolveAttributeLimits(): AttributeLimitsBehavior =
        resolvedBehavior.attributeLimits ?: AttributeLimitsBehavior()

    internal fun generateTracingConfig() =
        tracingConfig.generateTracingConfig(baseResource, resolveAttributeLimits())

    internal fun generateLoggingConfig() =
        loggingConfig.generateLoggingConfig(baseResource, resolveAttributeLimits())

    internal fun generateMetricsConfig() =
        metricsConfig.generateMetricsConfig(baseResource)
}
