package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer

/**
 * Allows links to be added to a span.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/
 */
@ExperimentalApi
@ThreadSafe
public interface SpanLinkMutator {

    /**
     * Adds a link to the span that associates it with another [SpanContext].
     */
    public fun addLink(spanContext: SpanContext, attributes: (MutableAttributeContainer.() -> Unit)? = null)
}
