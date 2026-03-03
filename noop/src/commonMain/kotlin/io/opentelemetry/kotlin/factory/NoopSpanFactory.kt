package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.NoopSpan
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext

internal object NoopSpanFactory : SpanFactory {
    override val invalid: Span = NoopSpan
    override fun fromSpanContext(spanContext: SpanContext): Span = NoopSpan
    override fun fromContext(context: Context): Span = NoopSpan
}
