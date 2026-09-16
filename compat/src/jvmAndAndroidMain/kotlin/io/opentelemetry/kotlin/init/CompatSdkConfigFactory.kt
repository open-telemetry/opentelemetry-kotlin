package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.factory.CompatResourceFactory
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.toIdGenerator
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.detectResource
import io.opentelemetry.kotlin.tracing.TracerProvider

/**
 * [OpenTelemetryBehavior] should be preferred to using this class. This will be removed eventually.
 */
@ExperimentalApi
internal class CompatSdkConfigFactory(
    private val cfg: CompatOpenTelemetryConfig,
    behavior: OpenTelemetryBehavior,
    private val clock: Clock,
    private val contextFactory: ContextFactory,
) {

    val idGenerator: IdGenerator =
        behavior.tracerProvider?.idGenerator?.toIdGenerator()
            ?: CompatIdGenerator()

    val spanLimits: SpanLimitsBehavior =
        behavior.tracerProvider?.spanLimits ?: SpanLimitsBehavior()

    val logLimits: LogLimitsBehavior =
        behavior.loggerProvider?.logLimits ?: LogLimitsBehavior()

    val baseResource: Resource = cfg.resourceDetectionConfig.detectors
        .detectResource(CompatResourceFactory, cfg.sdkErrorHandler)
        .merge(cfg.buildDeclaredResource())

    fun buildTracerProvider(): TracerProvider =
        cfg.tracerProviderConfig.build(clock, idGenerator, baseResource, spanLimits, contextFactory)

    fun buildLoggerProvider(): LoggerProvider =
        cfg.loggerProviderConfig.build(clock, baseResource, logLimits)

    fun buildMeterProvider(): MeterProvider =
        cfg.meterProviderConfig.build(clock, baseResource)
}
