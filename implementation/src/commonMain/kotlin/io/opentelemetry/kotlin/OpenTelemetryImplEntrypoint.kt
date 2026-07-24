package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.BaggageFactoryImpl
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.ResourceFactoryImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
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
    val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler
    val cfg = OpenTelemetryConfigImpl(clock, sdkErrorHandler).apply(config)
    val idGenerator = cfg.resolveIdGenerator()

    val resourceFactory = ResourceFactoryImpl()
    val traceFlags = TraceFlagsFactoryImpl()
    val traceState = TraceStateFactoryImpl()
    val spanContext = SpanContextFactoryImpl(idGenerator, traceFlags, traceState)

    val span = SpanFactoryImpl(spanContext)
    val contextFactory = ContextFactoryImpl(span, cfg.contextConfig::generateStorage)
    cfg.propagatorCfg.installFactories(
        traceFlagsFactory = traceFlags,
        traceStateFactory = traceState,
        spanContextFactory = spanContext,
        spanFactory = span,
    )

    val tracingConfig = cfg.generateTracingConfig()
    val loggingConfig = cfg.generateLoggingConfig()
    val metricsConfig = cfg.generateMetricsConfig()
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
            contextFactory = contextFactory
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
