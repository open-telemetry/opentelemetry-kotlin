package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.kotlin.tracing.TracerProvider

/**
 * The main entry point for the OpenTelemetry API.
 */
@ExperimentalApi
public interface OpenTelemetry : SdkFactory {

    /**
     * The [TracerProvider] for creating [Tracer] instances.
     */
    public val tracerProvider: TracerProvider

    /**
     * The [LoggerProvider] for creating [Logger] instances.
     */
    public val loggerProvider: LoggerProvider

    /**
     * The [Clock] that will be used for obtaining timestamps by this instance.
     */
    public val clock: Clock
}
