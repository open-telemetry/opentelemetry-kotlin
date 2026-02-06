package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.resource.Resource

/**
 * A full representation of a span that contains all the data needed for exporting.
 */
@ExperimentalApi
public interface SpanData : SpanSchema {

    /**
     * The timestamp at which this span ended, in nanoseconds. If it has not ended null will return.
     */
    @ThreadSafe
    public val endTimestamp: Long?

    /**
     * The resource associated with the object
     */
    @ThreadSafe
    public val resource: Resource

    /**
     * The instrumentation scope information associated with the object
     */
    @ThreadSafe
    public val instrumentationScopeInfo: InstrumentationScopeInfo

    /**
     * Returns true if this span has ended.
     */
    @ThreadSafe
    public val hasEnded: Boolean
}
