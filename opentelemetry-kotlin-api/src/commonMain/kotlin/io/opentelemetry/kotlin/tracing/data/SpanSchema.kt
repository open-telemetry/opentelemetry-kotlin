package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanKind

/**
 * The core set of properties exposed by all interfaces that present a span, excluding related and owned objects like attributes.
 * Mutability of the properties will the determined by the underlying implementation.
 */
@ExperimentalApi
public interface SpanSchema : AttributeContainer {

    /**
     * The span name
     */
    @ThreadSafe
    public val name: String

    /**
     * The span status
     */
    @ThreadSafe
    public val status: StatusData

    /**
     * The parent span context.
     */
    @ThreadSafe
    public val parent: SpanContext

    /**
     * The span context that uniquely identifies this span.
     */
    @ThreadSafe
    public val spanContext: SpanContext

    /**
     * The kind of this span
     */
    @ThreadSafe
    public val spanKind: SpanKind

    /**
     * The timestamp at which this span started, in nanoseconds.
     */
    @ThreadSafe
    public val startTimestamp: Long

    /**
     * A list of events associated with the span.
     */
    @ThreadSafe
    public val events: List<EventData>

    /**
     * A list of links associated with the span.
     */
    @ThreadSafe
    public val links: List<LinkData>
}
