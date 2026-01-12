package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext

/**
 * A factory for creating Span instances.
 */
@ExperimentalApi
public interface SpanFactory {

    /**
     * An invalid span.
     */
    public val invalid: Span

    /**
     * Returns a span from the supplied [SpanContext]. If the span context has no span an invalid span
     * object will be returned.
     */
    public fun fromSpanContext(spanContext: SpanContext): Span

    /**
     * Returns a span from the supplied [Context]. If the context has no span an invalid span
     * object will be returned.
     */
    public fun fromContext(context: Context): Span
}
