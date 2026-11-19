package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ClockProvider
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class OtelJavaReadableSpanAdapterTest {

    @Test
    fun testPropertiesAreExposed() {
        val data = FakeSpanData(
            spanContext = FakeSpanContext.VALID,
            spanKind = SpanKind.SERVER,
            attributes = mapOf("str" to "value", "long" to 5L),
        )
        val adapter = OtelJavaReadableSpanAdapter(FakeReadableSpan(data))
        assertEquals(data.name, adapter.name)
        assertEquals(FakeSpanContext.VALID.spanId, adapter.spanContext.spanId)
        assertEquals(FakeSpanContext.VALID.traceId, adapter.spanContext.traceId)
        assertFalse(adapter.parentSpanContext.isValid)
        assertEquals(OtelJavaSpanKind.SERVER, adapter.kind)
        assertTrue(adapter.hasEnded())
        assertEquals(1000L, adapter.latencyNanos)
        assertEquals("name", adapter.instrumentationScopeInfo.name)
        assertEquals("version", adapter.instrumentationScopeInfo.version)
        assertEquals("value", adapter.getAttribute(OtelJavaAttributeKey.stringKey("str")))
        assertEquals(5L, adapter.getAttribute(OtelJavaAttributeKey.longKey("long")))
        assertNull(adapter.getAttribute(OtelJavaAttributeKey.stringKey("missing")))
        assertEquals(2, adapter.attributes.size())
    }

    @Test
    fun testGetAttributeWithMismatchedTypeReturnsNull() {
        val data = FakeSpanData(
            attributes = mapOf("x" to "str", "d" to 1.0, "bytes" to AnyValue.BytesValue(byteArrayOf(1))),
        )
        val adapter = OtelJavaReadableSpanAdapter(FakeReadableSpan(data))
        assertNull(adapter.getAttribute(OtelJavaAttributeKey.longKey("x")))
        assertNull(adapter.getAttribute(OtelJavaAttributeKey.stringArrayKey("x")))
        assertNull(adapter.getAttribute(OtelJavaAttributeKey.longKey("d")))
        assertEquals(1.0, adapter.getAttribute(OtelJavaAttributeKey.doubleKey("d")))
        assertEquals(OtelJavaValue.of(byteArrayOf(1)), adapter.getAttribute(OtelJavaAttributeKey.valueKey("bytes")))
    }

    @Test
    fun testToSpanData() {
        val data = FakeSpanData()
        val javaData = OtelJavaReadableSpanAdapter(FakeReadableSpan(data)).toSpanData()
        assertEquals(data.name, javaData.name)
        assertEquals(data.startTimestamp, javaData.startEpochNanos)
        assertEquals(data.endTimestamp, javaData.endEpochNanos)
        assertEquals("value", javaData.attributes.get(OtelJavaAttributeKey.stringKey("key")))
        assertEquals(data.events.size, javaData.events.size)
        assertEquals(data.links.size, javaData.links.size)
    }

    @Test
    fun testInFlightLatencyUsesSpanClock() {
        val span = InFlightSpan(FakeSpanData(startTimestamp = 1000)) { 1500 }
        assertEquals(500L, OtelJavaReadableSpanAdapter(span).latencyNanos)
    }

    private class InFlightSpan(
        private val data: SpanData,
        override val clock: Clock,
    ) : ReadableSpan, SpanData by data, ClockProvider {
        override val endTimestamp: Long? = null
        override fun toSpanData(): SpanData = data
    }

    private class FakeReadableSpan(private val data: SpanData) : ReadableSpan, SpanData by data {
        override fun toSpanData(): SpanData = data
    }
}
