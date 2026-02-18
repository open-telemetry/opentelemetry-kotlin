package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanRelationships

/**
 * A Tracer is responsible for creating spans.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#tracer
 */
@ExperimentalApi
@ThreadSafe
public interface Tracer {

    @Deprecated(
        "Deprecated.",
        ReplaceWith(
            expression = "startSpan(name, parentContext, spanKind, startTimestamp, action)",
            imports = ["io.opentelemetry.kotlin.tracing.model.SpanKind"]
        )
    )
    public fun createSpan(
        name: String,
        parentContext: Context? = null,
        spanKind: SpanKind = SpanKind.INTERNAL,
        startTimestamp: Long? = null,
        action: (SpanRelationships.() -> Unit)? = null
    ): Span

    /**
     * Creates a new span. A span must have a non-empty name, and can optionally include:
     *
     * @param parentContext - a context object containing the parent span. If this is not set
     * explicitly, the implicit context via [io.opentelemetry.kotlin.factory.ContextFactory.implicitContext] will be used.
     * @param spanKind - the kind of span. Defaults to [SpanKind.INTERNAL].
     * @param startTimestamp - the start time of the span in nanoseconds. Defaults to the current time.
     * @param action - an action that allows attributes, links, and events to be added to the span. It
     * is possible to add these after span creation too, but it is preferred to add them before if possible.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/api/#span
     */
    @ThreadSafe
    public fun startSpan(
        name: String,
        parentContext: Context? = null,
        spanKind: SpanKind = SpanKind.INTERNAL,
        startTimestamp: Long? = null,
        action: (SpanRelationships.() -> Unit)? = null
    ): Span
}
