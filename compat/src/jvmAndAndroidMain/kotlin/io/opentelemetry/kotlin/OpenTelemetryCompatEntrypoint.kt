package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.factory.createCompatSdkFactory
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
    return createCompatOpenTelemetryImpl(
        clock,
        config,
        createCompatSdkFactory(),
    )
}

/**
 * Internal implementation of [createCompatOpenTelemetry]. This is not publicly visible as
 * we don't want to allow users to supply a custom [SdkFactory].
 */
@ExperimentalApi
internal fun createCompatOpenTelemetryImpl(
    clock: Clock,
    config: OpenTelemetryConfigDsl.() -> Unit,
    sdkFactory: SdkFactory,
): OpenTelemetry {
    val cfg = CompatOpenTelemetryConfig(clock, sdkFactory).apply(config)
    return OpenTelemetryImpl(
        tracerProvider = cfg.tracerProviderConfig.build(clock),
        loggerProvider = cfg.loggerProviderConfig.build(clock),
        clock = clock,
        sdkFactory = sdkFactory,
    )
}
