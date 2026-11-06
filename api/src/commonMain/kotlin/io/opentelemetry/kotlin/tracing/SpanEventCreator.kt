package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * Allows events to be added to a span.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/
 */
@ExperimentalApi
@ThreadSafe
public interface SpanEventCreator {

    /**
     * Adds an event to the span. [timestamp] is in nanoseconds since the Unix epoch. It defaults to 0,
     * which means the current time. Any value <= 0 is treated as the current time.
     */
    public fun addEvent(
        name: String,
        timestamp: Long = 0,
        attributes: (AttributesMutator.() -> Unit)? = null,
    )
}
