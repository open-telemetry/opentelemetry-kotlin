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

    /**
     * Factory that constructs tracing IDs.
     */
    public val tracingIdFactory: TracingIdFactory
}
