package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.factory.CompatBaggageFactory
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.CompatResourceFactory
import io.opentelemetry.kotlin.factory.CompatSpanContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.CompatTraceFlagsFactory
import io.opentelemetry.kotlin.factory.CompatTraceStateFactory
import io.opentelemetry.kotlin.init.CompatOpenTelemetryConfig
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl

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
    val traceFlags = CompatTraceFlagsFactory()
    val traceState = CompatTraceStateFactory()
    val spanContext = CompatSpanContextFactory()
    val contextFactory = CompatContextFactory()
    val span = CompatSpanFactory(spanContext)

    val cfg = CompatOpenTelemetryConfig(clock).apply(config)
    val resolvedIdGenerator = cfg.resolveIdGenerator()
    val base = cfg.buildGlobalResource()
    return CompatOpenTelemetryImpl(
        tracerProvider = cfg.tracerProviderConfig.build(clock, resolvedIdGenerator, base, cfg.globalAttributeLimits),
        loggerProvider = cfg.loggerProviderConfig.build(clock, base, cfg.globalAttributeLimits),
        meterProvider = cfg.meterProviderConfig.build(clock, base),
        clock = clock,
        spanContext = spanContext,
        traceFlags = traceFlags,
        traceState = traceState,
        context = contextFactory,
        span = span,
        baggage = CompatBaggageFactory(),
        idGenerator = resolvedIdGenerator,
        resource = CompatResourceFactory,
        propagator = cfg.propagatorCfg.buildPropagator(),
    )
}
