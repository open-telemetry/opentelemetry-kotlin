package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.tracing.StatusCode
import io.opentelemetry.kotlin.tracing.TracingDsl
import io.opentelemetry.kotlin.tracing.data.SpanSchema
import io.opentelemetry.kotlin.tracing.data.StatusData

/**
 * A span represents a single operation within a trace.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#span
 */
@TracingDsl
@ExperimentalApi
@ThreadSafe
public interface Span : SpanSchema, SpanRelationships {

    /**
     * Sets the name of the span. Must be non-empty.
     */
    @ThreadSafe
    public override var name: String

    /**
     * Sets the status of the span. This defaults to [StatusCode.UNSET].
     */
    @ThreadSafe
    public override var status: StatusData

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
     * Returns true if the span is currently recording.
     */
    @ThreadSafe
    public fun isRecording(): Boolean
}
