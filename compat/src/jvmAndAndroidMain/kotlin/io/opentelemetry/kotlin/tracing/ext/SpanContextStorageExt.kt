
package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import io.opentelemetry.kotlin.tracing.model.SpanAdapter

/**
 * Stores this span in the supplied [Context] and returns the new context, which is backed by an
 * opentelemetry-java context. A span not created by the compat layer is wrapped so that its span
 * context survives.
 */
@ExperimentalApi
public fun Span.storeInContext(context: Context): Context {
    return ContextAdapter(context.toOtelJavaContext().with(asOtelJavaSpan()))
}

/**
 * Spans created by this implementation wrap an opentelemetry-java span, so that is returned
 * directly. Any other implementation is wrapped so that its span context still survives.
 */
internal fun Span.asOtelJavaSpan(): OtelJavaSpan = (this as? SpanAdapter)?.impl ?: OtelJavaSpanAdapter(this)
