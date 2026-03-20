package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.StatusData

/**
 * A full representation of a span that contains all the data needed for exporting.
 */
@ExperimentalApi
public interface SpanData : AttributeContainer {

    /**
     * The span name.
     */
    @ThreadSafe
    public val name: String

    /**
     * The span status.
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
     * The kind of this span.
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
    public val events: List<SpanEventData>

    /**
     * A list of links associated with the span.
     */
    @ThreadSafe
    public val links: List<SpanLinkData>

    /**
     * The timestamp at which this span ended, in nanoseconds. If it has not ended null will return.
     */
    @ThreadSafe
    public val endTimestamp: Long?

    /**
     * The resource associated with the object.
     */
    @ThreadSafe
    public val resource: Resource

    /**
     * The instrumentation scope information associated with the object.
     */
    @ThreadSafe
    public val instrumentationScopeInfo: InstrumentationScopeInfo

    /**
     * Returns true if this span has ended.
     */
    @ThreadSafe
    public val hasEnded: Boolean
}
