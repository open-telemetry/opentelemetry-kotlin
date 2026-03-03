package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.factory.SdkFactoryImpl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.logging.LoggerProviderImpl
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
    return createOpenTelemetryImpl(clock, config)
}

/**
 * Internal implementation of [createOpenTelemetry]. This is not publicly visible as
 * we don't want to allow users to supply a custom [SdkFactory].
 */
@ExperimentalApi
internal fun createOpenTelemetryImpl(
    clock: Clock,
    config: OpenTelemetryConfigDsl.() -> Unit,
    idGenerator: IdGenerator = IdGeneratorImpl(),
): OpenTelemetry {
    val sdkFactory = SdkFactoryImpl(idGenerator)
    val cfg = OpenTelemetryConfigImpl(clock).apply(config)
    val tracingConfig = cfg.tracingConfig.generateTracingConfig()
    val loggingConfig = cfg.loggingConfig.generateLoggingConfig()
    return CloseableOpenTelemetryImpl(
        tracerProvider = TracerProviderImpl(clock, tracingConfig, sdkFactory),
        loggerProvider = LoggerProviderImpl(clock, loggingConfig, sdkFactory),
        clock = clock,
        sdkFactory = sdkFactory
    )
}
