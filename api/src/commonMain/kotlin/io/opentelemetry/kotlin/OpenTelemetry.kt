package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.metrics.Meter
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.propagation.Propagators
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.kotlin.tracing.TracerProvider

/**
 * The main entry point for the OpenTelemetry API.
 *
 * This contains interfaces in the SDK package and is intended for use by instrumentation
 * authors and application developers: https://opentelemetry.io/docs/specs/otel/overview/#api
 */
@ExperimentalApi
public interface OpenTelemetry : Propagators {

    /**
     * The [TracerProvider] for creating [Tracer] instances.
     */
    public val tracerProvider: TracerProvider

    /**
     * The [LoggerProvider] for creating [Logger] instances.
     */
    public val loggerProvider: LoggerProvider

    /**
     * The [MeterProvider] for creating [Meter] instances.
     */
    public val meterProvider: MeterProvider

    /**
     * Factory that constructs Span objects.
     */
    public val span: SpanFactory

    /**
     * Factory that constructs Context objects.
     */
    public val context: ContextFactory

    /**
     * The [TextMapPropagator] used to inject and extract context across process boundaries.
     */
    public val propagator: TextMapPropagator
}
