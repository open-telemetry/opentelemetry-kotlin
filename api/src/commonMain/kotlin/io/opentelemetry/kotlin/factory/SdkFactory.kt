package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Factory that constructs objects that are used within the SDK.
 */
@ExperimentalApi
public interface SdkFactory {

    /**
     * Factory that constructs SpanContext objects.
     */
    public val spanContextFactory: SpanContextFactory

    /**
     * Factory that constructs TraceFlags objects.
     */
    public val traceFlagsFactory: TraceFlagsFactory

    /**
     * Factory that constructs TraceState objects.
     */
    public val traceStateFactory: TraceStateFactory

    /**
     * Factory that constructs Context objects.
     */
    public val contextFactory: ContextFactory

    /**
     * Factory that constructs Span objects.
     */
    public val spanFactory: SpanFactory

    /**
     * Factory that constructs tracing IDs.
     */
    public val tracingIdFactory: TracingIdFactory
}
