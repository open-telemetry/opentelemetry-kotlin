package io.opentelemetry.kotlin.tracing.model

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
public interface SpanEventMutator {

    /**
     * Adds an event to the span.
     */
    public fun addEvent(
        name: String,
        timestamp: Long? = null,
        attributes: (AttributesMutator.() -> Unit)? = null,
    )
}
