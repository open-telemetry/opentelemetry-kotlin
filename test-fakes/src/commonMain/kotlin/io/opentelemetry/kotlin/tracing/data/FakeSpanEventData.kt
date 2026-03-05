package io.opentelemetry.kotlin.tracing.data
class FakeSpanEventData(
    override val name: String = "event",
    override val timestamp: Long = 1000,
    override val attributes: Map<String, Any> = mapOf("key" to "value")
) : SpanEventData
