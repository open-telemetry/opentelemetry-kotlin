package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.CompatResourceFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.factory.DefaultTraceFlagsFactory
import io.opentelemetry.kotlin.factory.DefaultTraceStateFactory
import io.opentelemetry.kotlin.init.CompatOpenTelemetryConfig
import io.opentelemetry.kotlin.init.CompatSdkConfigFactory
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.defaultCompatBehaviorReader

/**
 * Constructs an [OpenTelemetry] instance that exposes OpenTelemetry as a Kotlin API. The SDK is
 * configured entirely via the Kotlin DSL. Under the hood all calls to the Kotlin API will be
 * delegated to an OpenTelemetry Java SDK implementation that this SDK will construct internally.
 *
 * It's not possible to obtain a reference to the Java API using this function. If this is a
 * requirement because you have existing instrumentation, it's recommended to call
 * [toOtelKotlinApi] instead.
 */
@ExperimentalApi
public fun createCompatOpenTelemetry(
    clock: Clock = ClockAdapter(io.opentelemetry.sdk.common.Clock.getDefault()),
    config: OpenTelemetryConfigDsl.() -> Unit = {}
): OpenTelemetry {
    val traceFlags = DefaultTraceFlagsFactory
    val traceState = DefaultTraceStateFactory
    val spanContext = DefaultSpanContextFactory
    val contextFactory = CompatContextFactory()
    val span = CompatSpanFactory(spanContext)

    val cfg = CompatOpenTelemetryConfig(clock).apply(config)
    if (cfg.contextConfigured) {
        cfg.sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "OpenTelemetryConfigDsl.context",
                message = "Context configuration is ignored in compat mode. The implicit context is stored in " +
                    "opentelemetry-java's ContextStorage.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }
    val behavior = defaultCompatBehaviorReader(sdkErrorHandler = cfg.sdkErrorHandler)
        .read(configFilePath = cfg.configFilePath, dsl = cfg::toBehavior)

    // configFactory is legacy - use behavior to control SDK functionality instead
    val configFactory = CompatSdkConfigFactory(cfg, behavior, clock, contextFactory)
    return CompatOpenTelemetryImpl(
        tracerProvider = configFactory.buildTracerProvider(),
        loggerProvider = configFactory.buildLoggerProvider(),
        meterProvider = configFactory.buildMeterProvider(),
        clock = clock,
        spanContext = spanContext,
        traceFlags = traceFlags,
        traceState = traceState,
        context = contextFactory,
        span = span,
        idGenerator = configFactory.idGenerator,
        resource = CompatResourceFactory,
        propagator = cfg.propagatorCfg.buildPropagator(),
        sdkErrorHandler = cfg.sdkErrorHandler,
    )
}
