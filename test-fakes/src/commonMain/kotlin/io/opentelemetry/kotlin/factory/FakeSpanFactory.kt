package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanContext

@OptIn(ExperimentalApi::class)
internal class FakeSpanFactory : SpanFactory {
    override val invalid: Span = FakeSpan()
    override fun fromSpanContext(spanContext: SpanContext): Span = FakeSpan()
    override fun fromContext(context: Context): Span = FakeSpan()
}
