package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * Allows links to be added to a span.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/
 */
@ExperimentalApi
@ThreadSafe
public interface SpanLinkCreator {

    /**
     * Adds a link to the span that associates it with another [SpanContext].
     *
     * If [spanContext] is invalid, the link is only recorded if it has attributes or a non-empty
     * trace state.
     * See https://opentelemetry.io/docs/specs/otel/trace/api/#link
     */
    public fun addLink(spanContext: SpanContext, attributes: (AttributesMutator.() -> Unit)? = null)
}
