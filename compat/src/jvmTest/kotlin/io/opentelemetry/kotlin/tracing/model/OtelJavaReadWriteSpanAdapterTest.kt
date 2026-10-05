package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.StatusCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class OtelJavaReadWriteSpanAdapterTest {

    @Test
    fun testReadProperties() {
        val span = FakeReadWriteSpan(name = "my_span", spanContext = FakeSpanContext.VALID)
        val adapter = OtelJavaReadWriteSpanAdapter(span)
        assertEquals("my_span", adapter.name)
        assertEquals(FakeSpanContext.VALID.spanId, adapter.spanContext.spanId)
        assertTrue(adapter.isRecording)
    }

    @Test
    fun testMutationsAreForwarded() {
        val span = FakeReadWriteSpan(name = "my_span")
        val adapter = OtelJavaReadWriteSpanAdapter(span)
        adapter.updateName("renamed")
        adapter.setStatus(OtelJavaStatusCode.ERROR, "whoops")

        assertEquals("renamed", span.name)
        assertEquals("renamed", adapter.name)
        assertEquals(StatusCode.ERROR, span.status.statusCode)
        assertEquals("whoops", span.status.description)
    }
}
