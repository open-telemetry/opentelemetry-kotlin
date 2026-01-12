@file:OptIn(ExperimentalApi::class)

package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanAdapter

/**
 * Stores a span in the supplied [Context], returning the new context.
 */
public fun Span.storeInContext(context: Context): Context {
    val otelJavaCtx = (context as? ContextAdapter)?.impl ?: OtelJavaContext.root()
    val otelJavaSpan = this as? SpanAdapter ?: OtelJavaSpan.getInvalid()
    return ContextAdapter(otelJavaCtx.with(otelJavaSpan))
}
