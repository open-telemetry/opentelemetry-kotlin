package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.contextimpl.createInvalidSpanContext

public class SpanFactoryImpl : SpanFactory {

    private val invalidSpanContext = createInvalidSpanContext()

    override val invalid: Span by lazy { NonRecordingSpan(invalidSpanContext, invalidSpanContext) }

    override fun fromSpanContext(spanContext: SpanContext): Span =
        NonRecordingSpan(invalidSpanContext, spanContext)
}
