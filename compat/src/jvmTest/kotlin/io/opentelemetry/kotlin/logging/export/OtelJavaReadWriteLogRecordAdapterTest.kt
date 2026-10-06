package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class OtelJavaReadWriteLogRecordAdapterTest {

    @Test
    fun testPropertiesAreExposed() {
        val log = FakeReadWriteLogRecord(
            timestamp = 5,
            observedTimestamp = 10,
            severityNumber = SeverityNumber.WARN,
            severityText = "warning",
            body = "my_log",
            eventName = "my_event",
            spanContext = FakeSpanContext.VALID,
            attributes = mapOf("str" to "value", "long" to 5L),
        )
        val adapter = OtelJavaReadWriteLogRecordAdapter(log)
        assertEquals(5L, adapter.timestampEpochNanos)
        assertEquals(10L, adapter.observedTimestampEpochNanos)
        assertEquals(OtelJavaSeverity.WARN, adapter.severity)
        assertEquals("warning", adapter.severityText)
        assertEquals("my_event", adapter.eventName)
        assertEquals(FakeSpanContext.VALID.spanId, adapter.spanContext.spanId)
        assertEquals("name", adapter.instrumentationScopeInfo.name)
        assertEquals("value", adapter.getAttribute(OtelJavaAttributeKey.stringKey("str")))
        assertEquals(5L, adapter.getAttribute(OtelJavaAttributeKey.longKey("long")))
        assertNull(adapter.getAttribute(OtelJavaAttributeKey.stringKey("missing")))
        assertEquals(2, adapter.attributes.size())
        assertEquals("my_log", adapter.bodyValue?.asString())
    }

    @Test
    fun testMissingValuesDegradeToDefaults() {
        val adapter = OtelJavaReadWriteLogRecordAdapter(FakeReadWriteLogRecord(severityNumber = null))
        assertEquals(0L, adapter.timestampEpochNanos)
        assertEquals(0L, adapter.observedTimestampEpochNanos)
        assertEquals(OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER, adapter.severity)
        assertNull(adapter.severityText)
        assertNull(adapter.eventName)
    }

    @Test
    fun testNullBodyValue() {
        assertNull(OtelJavaReadWriteLogRecordAdapter(FakeReadWriteLogRecord(body = null)).bodyValue)
    }

    @Test
    fun testToLogRecordData() {
        val log = FakeReadWriteLogRecord(timestamp = 5, body = "my_log", attributes = mapOf("key" to "value"))
        val data = OtelJavaReadWriteLogRecordAdapter(log).toLogRecordData()
        assertEquals(5L, data.timestampEpochNanos)
        assertEquals("my_log", data.bodyValue?.asString())
        assertEquals("value", data.attributes.get(OtelJavaAttributeKey.stringKey("key")))
    }
}
