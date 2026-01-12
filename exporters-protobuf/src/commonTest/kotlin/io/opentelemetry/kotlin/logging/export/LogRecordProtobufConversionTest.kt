package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.assertAttributesMatch
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
class LogRecordProtobufConversionTest {

    @Test
    fun testEmptyConversion() {
        val obj = FakeReadableLogRecord(
            timestamp = null,
            observedTimestamp = null,
            severityNumber = null,
            severityText = null,
            body = null,
            attributes = emptyMap(),
        )
        val protobuf = obj.toProtobuf()
        assertEquals(0, protobuf.time_unix_nano)
        assertEquals(0, protobuf.observed_time_unix_nano)
        assertNull(protobuf.body)
        assertEquals(obj.spanContext.traceId, protobuf.trace_id.toByteArray().toHexString())
        assertEquals(obj.spanContext.spanId, protobuf.span_id.toByteArray().toHexString())
        assertEquals("", protobuf.severity_text)
        assertEquals(0, protobuf.severity_number.ordinal)
        assertAttributesMatch(obj.attributes, protobuf.attributes)
    }

    @Test
    fun testNonDefaultConversion() {
        val attrs = mapOf(
            "string" to "value",
            "long" to 5L,
            "double" to 10.0,
            "bool" to true,
            "stringList" to listOf("a", "b"),
            "longList" to listOf(5, 10L),
            "doubleList" to listOf(6.0, 12.0),
            "boolList" to listOf(true, false),
        )
        val obj = FakeReadableLogRecord(attributes = attrs)
        val protobuf = obj.toProtobuf()
        assertEquals(obj.timestamp, protobuf.time_unix_nano)
        assertEquals(obj.observedTimestamp, protobuf.observed_time_unix_nano)
        assertEquals(obj.body, protobuf.body?.string_value)
        assertEquals(obj.spanContext.traceId, protobuf.trace_id.toByteArray().toHexString())
        assertEquals(obj.spanContext.spanId, protobuf.span_id.toByteArray().toHexString())
        assertEquals(obj.severityText, protobuf.severity_text)
        assertEquals(obj.severityNumber?.severityNumber, protobuf.severity_number.value)
        assertAttributesMatch(obj.attributes, protobuf.attributes)
    }
}
