package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaClock
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.factory.CompatResourceFactory
import io.opentelemetry.kotlin.factory.CompatSpanContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.CompatTraceFlagsFactory
import io.opentelemetry.kotlin.factory.CompatTraceStateFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.logging.LoggerProviderAdapter
import io.opentelemetry.kotlin.tracing.TracerProviderAdapter

/**
 * Constructs an [OpenTelemetry] instance that exposes OpenTelemetry as a Kotlin API.
 * Callers must pass a reference to an OpenTelemetry Java SDK instance. Under the hood calls to the
 * Kotlin API will be delegated to the Java SDK implementation.
 *
 * This function is useful if you have existing OpenTelemetry Java SDK code that you don't want
 * to/can't rewrite, but still wish to use the Kotlin API for new code. It is permitted to call
 * both the Kotlin and Java APIs throughout the lifecycle of your application, although it would
 * generally be encouraged to migrate to [createCompatOpenTelemetry] as a long-term goal.
 */
@ExperimentalApi
public fun OtelJavaOpenTelemetry.toOtelKotlinApi(): OpenTelemetry {
    val idGenerator = CompatIdGenerator()
    val traceFlags = CompatTraceFlagsFactory()
    val traceState = CompatTraceStateFactory()
    val spanContext = CompatSpanContextFactory()
    val contextFactory = CompatContextFactory()
    val span = CompatSpanFactory(spanContext)
    val clock = ClockAdapter(OtelJavaClock.getDefault())
    return CompatOpenTelemetryImpl(
        tracerProvider = TracerProviderAdapter(tracerProvider, clock, CompatSpanLimitsConfig()),
        loggerProvider = LoggerProviderAdapter(logsBridge),
        clock = clock,
        spanContext = spanContext,
        traceFlags = traceFlags,
        traceState = traceState,
        context = contextFactory,
        span = span,
        idGenerator = idGenerator,
        resource = CompatResourceFactory,
    )
}
