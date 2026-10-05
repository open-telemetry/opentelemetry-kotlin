
package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import io.opentelemetry.kotlin.tracing.model.SpanAdapter

/**
 * Stores a span in the supplied [Context], returning the new context.
 */
public fun Span.storeInContext(context: Context): Context {
    return ContextAdapter(context.toOtelJavaContext().with(toOtelJavaSpan()))
}

/**
 * Spans created by this implementation wrap an opentelemetry-java span, so that is returned
 * directly. Any other implementation is wrapped so that its span context still survives.
 */
internal fun Span.toOtelJavaSpan(): OtelJavaSpan = (this as? SpanAdapter)?.impl ?: OtelJavaSpanAdapter(this)
