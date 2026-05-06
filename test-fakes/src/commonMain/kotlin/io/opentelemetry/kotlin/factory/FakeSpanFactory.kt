package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext

class FakeSpanFactory : SpanFactory {
    override val invalid: Span = FakeSpan()
    override fun fromSpanContext(spanContext: SpanContext): Span = FakeSpan()
}
