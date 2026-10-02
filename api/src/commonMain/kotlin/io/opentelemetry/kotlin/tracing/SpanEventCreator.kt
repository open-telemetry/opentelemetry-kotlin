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
     * Adds an event to the span. The timestamp defaults to the current time if null or <= 0.
     */
    public fun addEvent(
        name: String,
        timestamp: Long? = null,
        attributes: (AttributesMutator.() -> Unit)? = null,
    )
}
