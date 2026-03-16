package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.tracing.TracingDsl
import io.opentelemetry.kotlin.tracing.data.StatusData

/**
 * A span represents a single operation within a trace.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#span
 */
@TracingDsl
@ExperimentalApi
@ThreadSafe
public interface Span : AttributesMutator, SpanLinkCreator, SpanEventCreator {

    /**
     * The span context that uniquely identifies this span.
     */
    @ThreadSafe
    public val spanContext: SpanContext

    /**
     * The parent span context, or an invalid SpanContext if this is a root span.
     */
    @ThreadSafe
    public val parent: SpanContext

    /**
     * Returns true if the span is currently recording.
     */
    @ThreadSafe
    public fun isRecording(): Boolean

    /**
     * Sets the name of the span. Must be non-empty.
     */
    @ThreadSafe
    public fun setName(name: String)

    /**
     * Sets the status of the span.
     */
    @ThreadSafe
    public fun setStatus(status: StatusData)

    /**
     * Ends the span.
     */
    @ThreadSafe
    public fun end()

    /**
     * Ends the span, setting an explicit end-time in nanoseconds.
     */
    @ThreadSafe
    public fun end(timestamp: Long)

    /**
     * Records an exception on this span as an event, following the OpenTelemetry
     * semantic conventions for exception events.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/exceptions/
     */
    @ThreadSafe
    public fun recordException(
        exception: Throwable,
        attributes: (AttributesMutator.() -> Unit)? = null,
    )
}
