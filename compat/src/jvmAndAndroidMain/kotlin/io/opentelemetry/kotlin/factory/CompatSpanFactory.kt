package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext

internal class CompatSpanFactory(spanContextFactory: SpanContextFactory) : SpanFactory {

    private val invalidSpanContext by lazy { spanContextFactory.invalid }

    override val invalid: Span by lazy { NonRecordingSpan(invalidSpanContext, invalidSpanContext) }

    override fun fromSpanContext(spanContext: SpanContext): Span = when {
        spanContext.isValid -> NonRecordingSpan(invalidSpanContext, spanContext)
        else -> invalid
    }
}
