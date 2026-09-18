package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.BaggageFactoryImpl
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
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
        .read(configFilePath = cfg.configFilePath, dsl = cfg.toBehavior())

    // configFactory is legacy - use behavior to control SDK functionality instead
    val configFactory = SdkConfigFactory(cfg, behavior, resourceFactory)
    val idGenerator = configFactory.idGenerator

    val traceFlags = TraceFlagsFactoryImpl()
    val traceState = TraceStateFactoryImpl()
    val spanContext = SpanContextFactoryImpl(traceFlags, traceState)

    val span = SpanFactoryImpl(spanContext)
    val contextFactory = ContextFactoryImpl(span, cfg.sdkErrorHandler, cfg.contextConfig::generateStorage)
    cfg.propagatorCfg.installFactories(
        traceFlagsFactory = traceFlags,
        traceStateFactory = traceState,
        spanContextFactory = spanContext,
        spanFactory = span,
        sdkErrorHandler = cfg.sdkErrorHandler,
    )

    val tracingConfig = configFactory.generateTracingConfig()
    val loggingConfig = configFactory.generateLoggingConfig()
    val metricsConfig = configFactory.generateMetricsConfig()
    return OpenTelemetryImpl(
        tracerProvider = TracerProviderImpl(
            clock = clock,
            tracingConfig = tracingConfig,
            contextFactory = contextFactory,
            spanContextFactory = spanContext,
            traceFlagsFactory = traceFlags,
            spanFactory = span,
            idGenerator = idGenerator,
        ),
        loggerProvider = LoggerProviderImpl(
            clock = clock,
            loggingConfig = loggingConfig,
            contextFactory = contextFactory,
            spanContextFactory = spanContext,
        ),
        meterProvider = MeterProviderImpl(
            metricsConfig = metricsConfig,
        ),
        clock = clock,
        spanContext = spanContext,
        traceFlags = traceFlags,
        traceState = traceState,
        context = contextFactory,
        span = span,
        baggage = BaggageFactoryImpl(),
        idGenerator = idGenerator,
        resource = resourceFactory,
        propagator = cfg.propagatorCfg.buildPropagator(),
    )
}
