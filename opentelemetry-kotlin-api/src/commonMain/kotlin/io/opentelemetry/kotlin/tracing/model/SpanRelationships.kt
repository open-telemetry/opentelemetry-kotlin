package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer

/**
 * Provides operations that add relationships (events + links) to a span
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/
 */
@ExperimentalApi
public interface SpanRelationships : MutableAttributeContainer {

    /**
     * Adds a link to the span that associates it with another [SpanContext].
     */
    @ThreadSafe
    public fun addLink(spanContext: SpanContext, attributes: (MutableAttributeContainer.() -> Unit)? = null)

    /**
     * Adds an event to the span.
     */
    @ThreadSafe
    public fun addEvent(
        name: String,
        timestamp: Long? = null,
        attributes: (MutableAttributeContainer.() -> Unit)? = null,
    )
}
