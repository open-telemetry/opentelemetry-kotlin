package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
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
        behavior.tracerProvider?.idGenerator?.toIdGenerator()
            ?: IdGeneratorImpl()

    private val spanLimits: SpanLimitsBehavior =
        behavior.tracerProvider?.spanLimits ?: SpanLimitsBehavior()

    private val logLimits: LogLimitsBehavior =
        behavior.loggerProvider?.logLimits ?: LogLimitsBehavior()

    private val sampler: SamplerBehavior? = behavior.tracerProvider?.sampler

    private val processor: SpanProcessorBehavior? = behavior.tracerProvider?.processor

    private val baseResource = sdkDefaultResource()
        .merge(cfg.resourceDetectionConfig.detectors.detectResource(resourceFactory, cfg.sdkErrorHandler))
        .merge(cfg.globalResourceConfig.generateResource())

    fun generateTracingConfig(): TracingConfig {
        cfg.tracingConfig.applyResolvedSampler(sampler)
        cfg.tracingConfig.applyResolvedProcessor(processor)
        return cfg.tracingConfig.generateTracingConfig(baseResource, spanLimits)
    }

    fun generateLoggingConfig(): LoggingConfig =
        cfg.loggingConfig.generateLoggingConfig(baseResource, logLimits)

    fun generateMetricsConfig(): MetricsConfig =
        cfg.metricsConfig.generateMetricsConfig(baseResource)
}
