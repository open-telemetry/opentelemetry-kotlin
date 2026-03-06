package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.TraceFlagsFactory
import io.opentelemetry.kotlin.factory.TraceStateFactory
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.Tracer
import io.opentelemetry.kotlin.tracing.TracerProvider

/**
 * The main entry point for the OpenTelemetry API.
 *
 * This contains interfaces in the SDK package and is intended for use by instrumentation
 * authors and application developers: https://opentelemetry.io/docs/specs/otel/overview/#api
 */
@ExperimentalApi
public interface OpenTelemetry {

    /**
     * The [TracerProvider] for creating [Tracer] instances.
     */
    public val tracerProvider: TracerProvider

    /**
     * The [LoggerProvider] for creating [Logger] instances.
     */
    public val loggerProvider: LoggerProvider

    /**
     * Factory that constructs SpanContext objects.
     */
    public val spanContext: SpanContextFactory

    /**
     * Factory that constructs TraceFlags objects.
     */
    public val traceFlags: TraceFlagsFactory

    /**
     * Factory that constructs TraceState objects.
     */
    public val traceState: TraceStateFactory

    /**
     * Factory that constructs Context objects.
     */
    public val context: ContextFactory

    /**
     * Factory that constructs Span objects.
     */
    public val span: SpanFactory
}
