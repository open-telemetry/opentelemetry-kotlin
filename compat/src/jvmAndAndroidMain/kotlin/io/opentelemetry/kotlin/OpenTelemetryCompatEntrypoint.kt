package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.factory.CompatResourceFactory
import io.opentelemetry.kotlin.factory.CompatSpanContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.CompatTraceFlagsFactory
import io.opentelemetry.kotlin.factory.CompatTraceStateFactory
import io.opentelemetry.kotlin.factory.IdGenerator
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
    return createCompatOpenTelemetryImpl(clock, config)
}

/**
 * Internal implementation of [createCompatOpenTelemetry]. This is not publicly visible as
 * we don't want to allow users to supply a custom [IdGenerator].
 */
@ExperimentalApi
internal fun createCompatOpenTelemetryImpl(
    clock: Clock,
    config: OpenTelemetryConfigDsl.() -> Unit,
    idGenerator: IdGenerator = CompatIdGenerator(),
): OpenTelemetry {
    val traceFlags = CompatTraceFlagsFactory()
    val traceState = CompatTraceStateFactory()
    val spanContext = CompatSpanContextFactory()
    val contextFactory = CompatContextFactory()
    val span = CompatSpanFactory(spanContext)

    val cfg = CompatOpenTelemetryConfig(clock, idGenerator).apply(config)
    return CompatOpenTelemetryImpl(
        tracerProvider = cfg.tracerProviderConfig.build(clock),
        loggerProvider = cfg.loggerProviderConfig.build(clock),
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
