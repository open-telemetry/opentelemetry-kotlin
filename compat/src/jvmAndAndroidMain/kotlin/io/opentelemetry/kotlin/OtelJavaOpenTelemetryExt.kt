package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaClock
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider
import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.logging.LoggerProviderAdapter
import io.opentelemetry.kotlin.metrics.MeterProviderAdapter
import io.opentelemetry.kotlin.propagation.TextMapPropagatorAdapter
import io.opentelemetry.kotlin.tracing.TracerProviderAdapter

/**
 * Constructs an [OpenTelemetrySdk] instance that exposes OpenTelemetry as a Kotlin API.
 * Callers must pass a reference to an OpenTelemetry Java SDK instance. Under the hood calls to the
 * Kotlin API will be delegated to the Java SDK implementation.
 *
 * This function is useful if you have existing OpenTelemetry Java SDK code that you don't want
 * to/can't rewrite, but still wish to use the Kotlin API for new code. It is permitted to call
 * both the Kotlin and Java APIs throughout the lifecycle of your application, although it would
 * generally be encouraged to migrate to [createCompatOpenTelemetry] as a long-term goal.
 *
 * The returned instance stores the current context in opentelemetry-java's `ContextStorage`, so the
 * Kotlin and Java APIs share one current context. Errors that `:api` reports go to [sdkErrorHandler]
 * unless another SDK already receives them. Calling [OpenTelemetrySdk.shutdown] shuts down the Java
 * SDK's providers and releases that process-wide state.
 *
 * @param clock the clock used to timestamp telemetry created via the Kotlin API.
 * @param sdkErrorHandler receives errors raised while delegating to the Java SDK, such as
 * exceptions thrown when flushing or shutting down its providers. Defaults to [PlatformSdkErrorHandler].
 */
@ExperimentalApi
public fun OtelJavaOpenTelemetry.toOtelKotlinApi(
    clock: Clock = ClockAdapter(OtelJavaClock.getDefault()),
    sdkErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler,
): OpenTelemetrySdk {
    val errorHandler = GuardedSdkErrorHandler(sdkErrorHandler)
    val contextFactory = CompatContextFactory()
    val processWideState = claimCompatProcessWideState("toOtelKotlinApi", errorHandler)
    return CompatOpenTelemetryImpl(
        tracerProvider = TracerProviderAdapter(
            unobfuscatedTracerProvider(),
            contextFactory,
            errorHandler,
        ),
        loggerProvider = LoggerProviderAdapter(unobfuscatedLoggerProvider(), errorHandler),
        meterProvider = MeterProviderAdapter(unobfuscatedMeterProvider(), errorHandler),
        clock = clock,
        context = contextFactory,
        propagator = TextMapPropagatorAdapter(propagators.textMapPropagator),
        sdkErrorHandler = errorHandler,
        processWideState = processWideState,
    )
}

/**
 * [OtelJavaOpenTelemetrySdk] hides its SDK providers behind obfuscated wrappers, which prevents
 * the adapters from delegating flush/shutdown to them. These functions return the SDK providers
 * where they are available.
 */
private fun OtelJavaOpenTelemetry.unobfuscatedTracerProvider(): OtelJavaTracerProvider = when (this) {
    is OtelJavaOpenTelemetrySdk -> sdkTracerProvider
    else -> tracerProvider
}

private fun OtelJavaOpenTelemetry.unobfuscatedLoggerProvider(): OtelJavaLoggerProvider = when (this) {
    is OtelJavaOpenTelemetrySdk -> sdkLoggerProvider
    else -> logsBridge
}

private fun OtelJavaOpenTelemetry.unobfuscatedMeterProvider(): OtelJavaMeterProvider = when (this) {
    is OtelJavaOpenTelemetrySdk -> sdkMeterProvider
    else -> meterProvider
}
