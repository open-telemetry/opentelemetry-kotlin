package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.factory.toIdGenerator
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.resource.detectResource

/**
 * [OpenTelemetryBehavior] should be preferred to using this class. This will be removed eventually.
 */
internal class SdkConfigFactory(
    private val cfg: OpenTelemetryConfigImpl,
    behavior: OpenTelemetryBehavior,
    resourceFactory: ResourceFactory = ResourceFactoryImpl(),
) {

    val idGenerator: IdGenerator =
        cfg.customIdGenerator?.invoke()
            ?: behavior.tracerProvider?.idGenerator?.toIdGenerator()
            ?: IdGeneratorImpl()

    private val globalAttributeLimits: AttributeLimitsBehavior =
        behavior.attributeLimits ?: AttributeLimitsBehavior()

    private val spanLimits: SpanLimitsBehavior =
        behavior.tracerProvider?.spanLimits ?: SpanLimitsBehavior()

    private val logLimits: LogLimitsBehavior =
        behavior.loggerProvider?.logLimits ?: LogLimitsBehavior()

    private val sampler: SamplerBehavior? = behavior.tracerProvider?.sampler

    private val baseResource = sdkDefaultResource()
        .merge(cfg.resourceDetectionConfig.detectors.detectResource(resourceFactory, cfg.sdkErrorHandler))
        .merge(cfg.globalResourceConfig.generateResource())

    fun generateTracingConfig(): TracingConfig {
        cfg.tracingConfig.applyResolvedSampler(sampler)
        return cfg.tracingConfig.generateTracingConfig(baseResource, globalAttributeLimits, spanLimits)
    }

    fun generateLoggingConfig(): LoggingConfig =
        cfg.loggingConfig.generateLoggingConfig(baseResource, globalAttributeLimits, logLimits)

    fun generateMetricsConfig(): MetricsConfig =
        cfg.metricsConfig.generateMetricsConfig(baseResource)
}
