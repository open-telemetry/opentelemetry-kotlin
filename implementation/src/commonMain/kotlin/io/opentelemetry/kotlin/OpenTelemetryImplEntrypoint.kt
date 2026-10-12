package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.init.SdkConfigFactory
import io.opentelemetry.kotlin.init.defaultBehaviorReader
import io.opentelemetry.kotlin.logging.LoggerProviderImpl
import io.opentelemetry.kotlin.metrics.MeterProviderImpl
import io.opentelemetry.kotlin.tracing.TracerProviderImpl

/**
 * Constructs an [OpenTelemetry] instance that uses the opentelemetry-kotlin implementation.
 */
@ExperimentalApi
public fun createOpenTelemetry(

    /**
     * Defines the [Clock] implementation used by OpenTelemetry.
     */
    clock: Clock = ClockImpl(),

    /**
     * Defines configuration for OpenTelemetry.
     */
    config: OpenTelemetryConfigDsl.() -> Unit = {}
): OpenTelemetry {
    val resourceFactory = ResourceFactoryImpl()
    val cfg = OpenTelemetryConfigImpl(clock).apply(config)
    val behavior = defaultBehaviorReader(sdkErrorHandler = cfg.sdkErrorHandler)
        .read(configFilePath = cfg.configFilePath, dsl = cfg::toBehavior)

    // configFactory is legacy - use behavior to control SDK functionality instead
    val configFactory = SdkConfigFactory(cfg, behavior, resourceFactory)
    val idGenerator = configFactory.idGenerator

    val span = SpanFactoryImpl()
    val contextFactory = ContextFactoryImpl(span, cfg.sdkErrorHandler, cfg.contextConfig::generateStorage)
    cfg.propagatorCfg.installFactories(spanFactory = span)

    val tracingConfig = configFactory.generateTracingConfig()
    val loggingConfig = configFactory.generateLoggingConfig()
    val metricsConfig = configFactory.generateMetricsConfig()
    return OpenTelemetryImpl(
        tracerProvider = TracerProviderImpl(
            clock = clock,
            tracingConfig = tracingConfig,
            contextFactory = contextFactory,
            spanFactory = span,
            idGenerator = idGenerator,
            attributeLimits = behavior.attributeLimits ?: AttributeLimitsBehavior(),
            spanLimits = behavior.tracerProvider?.spanLimits ?: SpanLimitsBehavior(),
        ),
        loggerProvider = LoggerProviderImpl(
            clock = clock,
            loggingConfig = loggingConfig,
            contextFactory = contextFactory,
            attributeLimits = behavior.attributeLimits ?: AttributeLimitsBehavior(),
            logLimits = behavior.loggerProvider?.logLimits ?: LogLimitsBehavior(),
        ),
        meterProvider = MeterProviderImpl(
            metricsConfig = metricsConfig,
            attributeLimits = behavior.attributeLimits ?: AttributeLimitsBehavior()
        ),
        clock = clock,
        context = contextFactory,
        span = span,
        idGenerator = idGenerator,
        resource = resourceFactory,
        propagator = cfg.propagatorCfg.buildPropagator(),
    )
}
