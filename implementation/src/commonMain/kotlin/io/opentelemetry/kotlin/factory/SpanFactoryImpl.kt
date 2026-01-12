package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext

@OptIn(ExperimentalApi::class)
internal class SpanFactoryImpl(
    spanContextFactory: SpanContextFactory,
    private val spanKey: ContextKey<Span>
) : SpanFactory {

    private val invalidSpanContext by lazy { spanContextFactory.invalid }

    override val invalid: Span by lazy { NonRecordingSpan(invalidSpanContext, invalidSpanContext) }

    override fun fromSpanContext(spanContext: SpanContext): Span =
        NonRecordingSpan(invalidSpanContext, spanContext)

    override fun fromContext(context: Context): Span {
        return context.get(spanKey) ?: invalid
    }
}
