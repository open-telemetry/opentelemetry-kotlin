@file:Suppress("DiscouragedImport")

package io.opentelemetry.kotlin.factory

import io.opentelemetry.api.trace.otelJavaSpanContextKey
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextAdapter
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter

@OptIn(ExperimentalApi::class)
internal class CompatSpanFactory(spanContextFactory: SpanContextFactory) : SpanFactory {

    private val invalidSpanContext by lazy { spanContextFactory.invalid }

    override val invalid: Span by lazy { NonRecordingSpan(invalidSpanContext, invalidSpanContext) }

    override fun fromSpanContext(spanContext: SpanContext): Span = when {
        spanContext.isValid -> NonRecordingSpan(invalidSpanContext, spanContext)
        else -> invalid
    }

    override fun fromContext(context: Context): Span {
        val otelJavaCtx = (context as? ContextAdapter)?.impl ?: OtelJavaContext.root()
        val span: OtelJavaSpan =
            otelJavaCtx.get(otelJavaSpanContextKey) ?: OtelJavaSpan.getInvalid()
        return NonRecordingSpan(invalid.parent, SpanContextAdapter(span.spanContext))
    }
}
