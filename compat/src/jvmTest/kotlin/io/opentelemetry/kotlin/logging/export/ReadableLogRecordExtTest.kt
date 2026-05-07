package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ReadableLogRecordExtTest {

    @Test
    fun testLogRecordDefaultConversions() {
        val record = FakeReadableLogRecord()
        val observed = record.toLogRecordData()
        assertEquals(record.timestamp, observed.timestampEpochNanos)
        assertEquals(record.observedTimestamp, observed.observedTimestampEpochNanos)
        assertEquals(record.severityText, observed.severityText)
        assertEquals(OtelJavaSeverity.WARN, observed.severity)
        assertEquals(record.body, observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordStructuredBodyConversion() {
        val structuredBody = mapOf("key" to "value")
        val record = FakeReadableLogRecord(body = structuredBody)
        val observed = record.toLogRecordData()
        assertEquals(structuredBody.toString(), observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordAnyValueStringBody() {
        val record = FakeReadableLogRecord(body = AnyValue.StringValue("hello"))
        val observed = record.toLogRecordData()
        assertEquals("hello", observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordAnyValueMapBody() {
        val map = AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("v")))
        val record = FakeReadableLogRecord(body = map)
        val observed = record.toLogRecordData()
        assertEquals(map.toString(), observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordNullConversions() {
        val record = FakeReadableLogRecord(
            timestamp = null,
            observedTimestamp = null,
            severityNumber = null,
            severityText = null,
            body = null,
        )
        val observed = record.toLogRecordData()
        assertEquals(0, observed.timestampEpochNanos)
        assertEquals(0, observed.observedTimestampEpochNanos)
        assertEquals(OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER, observed.severity)
        assertNull(observed.severityText)
        assertNull(observed.bodyValue?.asString())
    }
}
