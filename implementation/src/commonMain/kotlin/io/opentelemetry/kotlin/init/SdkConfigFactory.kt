package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SamplerBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.ResourceFactory
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.factory.toIdGenerator
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.init.config.TracingConfig
import io.opentelemetry.kotlin.resource.SdkMode
import io.opentelemetry.kotlin.resource.detectResource
import io.opentelemetry.kotlin.resource.sdkDefaultResource

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

    private val sampler: SamplerBehavior? = behavior.tracerProvider?.sampler

    private val spanProcessor: SpanProcessorBehavior? = behavior.tracerProvider?.processor

    private val logProcessor: LogRecordProcessorBehavior? = behavior.loggerProvider?.processor

    private val baseResource = resourceFactory.sdkDefaultResource(SdkMode.REGULAR)
        .merge(cfg.resourceDetectionConfig.detectors.detectResource(resourceFactory, cfg.sdkErrorHandler))
        .merge(cfg.globalResourceConfig.toBehavior().toResource())

    fun generateTracingConfig(): TracingConfig {
        cfg.tracingConfig.applyResolvedSampler(sampler)
        return cfg.tracingConfig.generateTracingConfig(baseResource, spanProcessor)
    }

    fun generateLoggingConfig(): LoggingConfig =
        cfg.loggingConfig.generateLoggingConfig(baseResource, logProcessor)

    fun generateMetricsConfig(): MetricsConfig =
        cfg.metricsConfig.generateMetricsConfig(baseResource)
}
