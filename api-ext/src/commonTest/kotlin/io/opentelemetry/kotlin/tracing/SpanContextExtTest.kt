package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.toHexString
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanContextExtTest {

    @Test
    fun testSpanIdBytes() {
        val spanContext = FakeSpanContext.INVALID
        val expected = spanContext.spanId
        val observed = spanContext.spanIdBytes.toHexString()
        assertEquals(expected, observed)
    }

    @Test
    fun testTraceIdBytes() {
        val spanContext = FakeSpanContext.INVALID
        val expected = spanContext.traceId
        val observed = spanContext.traceIdBytes.toHexString()
        assertEquals(expected, observed)
    }
}
