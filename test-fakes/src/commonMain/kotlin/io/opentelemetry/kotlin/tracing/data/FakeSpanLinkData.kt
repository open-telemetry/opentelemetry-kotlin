package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext

class FakeSpanLinkData(
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
    override val attributes: Map<String, Any> = mapOf("key" to "value")
) : SpanLinkData
