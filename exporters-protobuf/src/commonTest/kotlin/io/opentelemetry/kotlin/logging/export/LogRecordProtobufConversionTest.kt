package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.export.assertAttributesMatch
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun testAnyValueStringBodySerialisesAsString() {
        val protobuf = FakeReadableLogRecord(body = AnyValue.StringValue("hi")).toProtobuf()
        assertEquals("hi", protobuf.body?.string_value)
    }

    @Test
    fun testAnyValueBoolBodySerialisesAsBool() {
        val protobuf = FakeReadableLogRecord(body = AnyValue.BoolValue(true)).toProtobuf()
        assertEquals(true, protobuf.body?.bool_value)
    }

    @Test
    fun testAnyValueLongBodySerialisesAsInt() {
        val protobuf = FakeReadableLogRecord(body = AnyValue.LongValue(42)).toProtobuf()
        assertEquals(42L, protobuf.body?.int_value)
    }

    @Test
    fun testAnyValueDoubleBodySerialisesAsDouble() {
        val protobuf = FakeReadableLogRecord(body = AnyValue.DoubleValue(1.5)).toProtobuf()
        assertEquals(1.5, protobuf.body?.double_value)
    }

    @Test
    fun testAnyValueBytesBodySerialisesAsBytes() {
        val bytes = byteArrayOf(1, 2, 3)
        val protobuf = FakeReadableLogRecord(body = AnyValue.BytesValue(bytes)).toProtobuf()
        assertContentEquals(bytes, protobuf.body?.bytes_value?.toByteArray())
    }

    @Test
    fun testAnyValueNullBodySerialisesAsEmptyAnyValue() {
        val protobuf = FakeReadableLogRecord(body = AnyValue.NullValue).toProtobuf()
        val any = protobuf.body
        assertNotNull(any)
        assertNull(any.string_value)
        assertNull(any.bool_value)
        assertNull(any.int_value)
        assertNull(any.double_value)
        assertNull(any.bytes_value)
        assertNull(any.array_value)
        assertNull(any.kvlist_value)
    }

    @Test
    fun testAnyValueListBodySerialisesAsArrayValue() {
        val list = AnyValue.ListValue(
            listOf(
                AnyValue.StringValue("a"),
                AnyValue.LongValue(1),
            )
        )
        val protobuf = FakeReadableLogRecord(body = list).toProtobuf()
        val arr = protobuf.body?.array_value
        assertNotNull(arr)
        assertEquals(2, arr.values.size)
        assertEquals("a", arr.values[0].string_value)
        assertEquals(1L, arr.values[1].int_value)
    }

    @Test
    fun testAnyValueMapBodySerialisesAsKeyValueList() {
        val map = AnyValue.MapValue(
            mapOf(
                "name" to AnyValue.StringValue("hello"),
                "count" to AnyValue.LongValue(7),
            )
        )
        val protobuf = FakeReadableLogRecord(body = map).toProtobuf()
        val kv = protobuf.body?.kvlist_value
        assertNotNull(kv)
        assertEquals(2, kv.values.size)
        val byKey = kv.values.associateBy { it.key }
        assertEquals("hello", byKey["name"]?.value_?.string_value)
        assertEquals(7L, byKey["count"]?.value_?.int_value)
    }

    @Test
    fun testAnyValueNestedMapBodyRoundTrips() {
        val original = AnyValue.MapValue(
            mapOf(
                "outer" to AnyValue.MapValue(
                    mapOf(
                        "inner" to AnyValue.ListValue(
                            listOf(
                                AnyValue.StringValue("x"),
                                AnyValue.BoolValue(false),
                            )
                        )
                    )
                )
            )
        )
        val obj = FakeReadableLogRecord(body = original)
        val protobuf = obj.toProtobuf()
        val restored = protobuf.toReadableLogRecord(obj.resource, obj.instrumentationScopeInfo)
        assertEquals(original, restored.body)
    }

    @Test
    fun testNonAnyValueObjectBodyFallsBackToToString() {
        val payload = object {
            override fun toString() = "custom-string"
        }
        val protobuf = FakeReadableLogRecord(body = payload).toProtobuf()
        assertEquals("custom-string", protobuf.body?.string_value)
    }

    @Test
    fun testPrimitiveBodyRoundTripsAsRawKotlinType() {
        val obj = FakeReadableLogRecord(body = 99L)
        val restored = obj.toProtobuf().toReadableLogRecord(obj.resource, obj.instrumentationScopeInfo)
        assertTrue(restored.body is Long, "expected Long, got ${restored.body?.let { it::class }}")
        assertEquals(99L, restored.body)
    }
}
