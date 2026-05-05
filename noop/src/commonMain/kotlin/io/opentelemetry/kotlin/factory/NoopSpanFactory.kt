package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.NoopSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext

internal object NoopSpanFactory : SpanFactory {
    override val invalid: Span = NoopSpan
    override fun fromSpanContext(spanContext: SpanContext): Span = NoopSpan
}
