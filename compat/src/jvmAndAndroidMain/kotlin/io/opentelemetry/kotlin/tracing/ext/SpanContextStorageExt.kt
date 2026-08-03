
package io.opentelemetry.kotlin.tracing.ext

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
    // spans created by this implementation wrap an opentelemetry-java span, so store that
    // directly. Any other implementation is wrapped so that its span context still survives.
    val otelJavaSpan = (this as? SpanAdapter)?.impl ?: OtelJavaSpanAdapter(this)
    return ContextAdapter(context.toOtelJavaContext().with(otelJavaSpan))
}
