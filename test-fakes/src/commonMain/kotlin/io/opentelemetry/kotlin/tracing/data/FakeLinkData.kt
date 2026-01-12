package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContext

@OptIn(ExperimentalApi::class)
class FakeLinkData(
    override val spanContext: SpanContext = FakeSpanContext.INVALID,
    override val attributes: Map<String, Any> = mapOf("key" to "value")
) : LinkData
