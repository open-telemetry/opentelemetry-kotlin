
package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import io.opentelemetry.kotlin.tracing.model.SpanAdapter

/**
 * Stores this span in the supplied [Context] and returns the new context. Equivalent to
 * [Context.storeSpan].
 */
@ExperimentalApi
public fun Span.storeInContext(context: Context): Context = context.storeSpan(this)

/**
 * Spans created by this implementation wrap an opentelemetry-java span, so that is returned
 * directly. Any other implementation is wrapped so that its span context still survives.
 */
internal fun Span.asOtelJavaSpan(): OtelJavaSpan = (this as? SpanAdapter)?.impl ?: OtelJavaSpanAdapter(this)
