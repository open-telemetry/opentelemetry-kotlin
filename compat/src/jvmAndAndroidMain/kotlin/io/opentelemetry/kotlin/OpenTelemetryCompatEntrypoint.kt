package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.guard
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.factory.CompatResourceFactory
import io.opentelemetry.kotlin.init.CompatOpenTelemetryConfig
import io.opentelemetry.kotlin.init.CompatSdkConfigFactory
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.defaultCompatBehaviorReader

/**
 * Constructs an [OpenTelemetrySdk] instance that exposes OpenTelemetry as a Kotlin API. The SDK is
 * configured entirely via the Kotlin DSL. Under the hood all calls to the Kotlin API will be
 * delegated to an OpenTelemetry Java SDK implementation that this SDK will construct internally.
 *
 * The caller owns the returned instance and should call [OpenTelemetrySdk.shutdown] when telemetry
 * is no longer needed.
 *
 * It's not possible to obtain a reference to the Java API using this function. If this is a
 * requirement because you have existing instrumentation, it's recommended to call
 * [toOtelKotlinApi] instead.
 */
@ExperimentalApi
public fun createCompatOpenTelemetry(
    clock: Clock = ClockAdapter(io.opentelemetry.sdk.common.Clock.getDefault()),
    config: (OpenTelemetryConfigDsl.() -> Unit)? = null
): OpenTelemetrySdk {
    val cfg = CompatOpenTelemetryConfig(clock)
    if (config != null) {
        cfg.sdkErrorHandler.guard("Failed to apply OpenTelemetry config, continuing with partial config") {
            cfg.config()
        }
    }
    if (cfg.contextConfigured) {
        cfg.sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "OpenTelemetryConfigDsl.context",
                message = "Context configuration is ignored in compat mode. The current context is stored in " +
                    "opentelemetry-java's ContextStorage.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }
    val behavior = defaultCompatBehaviorReader(
        sdkErrorHandler = cfg.sdkErrorHandler,
        declarativeConfigReader = cfg.declarativeConfigReader,
    ).read(configFilePath = cfg.configFilePath, dsl = cfg::toBehavior)

    if (behavior.disabled == true) {
        return NoopOpenTelemetrySdk
    }

    // configFactory is legacy - use behavior to control SDK functionality instead
    val configFactory = CompatSdkConfigFactory(cfg, behavior, clock)
    val tracerProvider = configFactory.buildTracerProvider()
    val loggerProvider = configFactory.buildLoggerProvider()
    val meterProvider = configFactory.buildMeterProvider()

    // claim process-wide state last, so that a failure earlier in init doesn't leave it claimed.
    val processWideState = claimCompatProcessWideState("createCompatOpenTelemetry", cfg.sdkErrorHandler)
    return CompatOpenTelemetryImpl(
        tracerProvider = tracerProvider,
        loggerProvider = loggerProvider,
        meterProvider = meterProvider,
        clock = clock,
        idGenerator = configFactory.idGenerator,
        resource = CompatResourceFactory,
        propagator = cfg.createPropagator(),
        sdkErrorHandler = cfg.sdkErrorHandler,
        processWideState = processWideState,
        timeoutMs = cfg.shutdownTimeoutMs,
    )
}
