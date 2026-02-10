package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.proto.common.v1.InstrumentationScope
import kotlin.collections.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
class CommonProtobufConversionTest {

    @Test
    fun testEmptyInstrumentationScopeConversion() {
        val obj = FakeInstrumentationScopeInfo("name", null, null, emptyMap())
        val protobuf = obj.toProtobuf()
        assertEquals(0, protobuf.attributes.size)
        assertEquals("name", protobuf.name)
        assertEquals("", protobuf.version)
    }

    @Test
    fun testInstrumentationConversionWithValues() {
        val obj = FakeInstrumentationScopeInfo(
            "custom_name",
            "0.1.0",
            "https://example.com/schema",
            mapOf("foo" to "bar")
        )
        val protobuf = obj.toProtobuf()
        assertEquals("custom_name", protobuf.name)
        assertEquals("0.1.0", protobuf.version)
        assertEquals(1, protobuf.attributes.size)
        val attribute = protobuf.attributes[0]
        assertEquals("foo", attribute.key)
        assertEquals("bar", attribute.value_?.string_value)
    }

    @Test
    fun testEmptyResourceConversion() {
        val obj = FakeResource(attributes = emptyMap())
        val protobuf = obj.toProtobuf()
        assertEquals(0, protobuf.attributes.size)
        assertEquals(0, protobuf.dropped_attributes_count)
    }

    @Test
    fun testResourceNonDefaultConversion() {
        val obj = FakeResource(
            attributes = mapOf(
                "string" to "foo"
            )
        )
        val protobuf = obj.toProtobuf()
        assertEquals(1, protobuf.attributes.size)
        assertEquals("foo", protobuf.attributes[0].value_?.string_value)
        assertEquals(0, protobuf.dropped_attributes_count)
    }

    @Test
    fun testInstrumentationScopeDeserialization() {
        val proto = InstrumentationScope(
            name = "test-scope",
            version = "1.0.0",
            attributes = emptyList()
        )
        val scope = proto.toInstrumentationScopeInfo("https://schema.url")
        assertEquals("test-scope", scope.name)
        assertEquals("1.0.0", scope.version)
        assertEquals("https://schema.url", scope.schemaUrl)
        assertEquals(0, scope.attributes.size)
    }

    @Test
    fun testInstrumentationScopeDeserialization_emptyVersion() {
        val proto = InstrumentationScope(
            name = "test-scope",
            version = "",
            attributes = emptyList()
        )
        val scope = proto.toInstrumentationScopeInfo(null)
        assertNull(scope.version)
        assertNull(scope.schemaUrl)
    }

    @Test
    fun testInstrumentationScopeDeserialization_emptySchemaUrl() {
        val proto = InstrumentationScope(
            name = "test-scope",
            version = "1.0.0",
            attributes = emptyList()
        )
        val scope = proto.toInstrumentationScopeInfo("")
        assertNull(scope.schemaUrl)
    }

    @Test
    fun testResourceDeserialization() {
        val proto = io.opentelemetry.proto.resource.v1.Resource(
            attributes = emptyList()
        )
        val resource = proto.toResource()
        assertEquals(0, resource.attributes.size)
    }

    @Test
    fun testResourceDeserialization_asNewResourceThrows() {
        val proto = io.opentelemetry.proto.resource.v1.Resource(
            attributes = emptyList()
        )
        val resource = proto.toResource()
        assertFailsWith<UnsupportedOperationException> {
            resource.asNewResource { }
        }
    }

    @Test
    fun testTraceFlagsConversion() {
        val flags = FakeTraceFlags(isSampled = true, isRandom = false, hex = "01")
        assertEquals(1, flags.toFlagsInt())
    }

    @Test
    fun testDeserializedSpanContext_valid() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            flags = 1
        )
        assertTrue(context.isValid)
        assertTrue(context.traceFlags.isSampled)
        assertFalse(context.isRemote)
        assertEquals("12345678901234567890123456789012", context.traceId)
        assertEquals("1234567890123456", context.spanId)
    }

    @Test
    fun testDeserializedSpanContext_invalid() {
        val context = DeserializedSpanContext(
            traceIdBytes = "00000000000000000000000000000000".hexToByteArray(),
            spanIdBytes = "0000000000000000".hexToByteArray()
        )
        assertFalse(context.isValid)
    }

    @Test
    fun testDeserializedSpanContext_withTraceState() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceStateString = "key1=value1,key2=value2"
        )
        assertEquals("value1", context.traceState.get("key1"))
        assertEquals("value2", context.traceState.get("key2"))
        assertEquals(2, context.traceState.asMap().size)
    }

    @Test
    fun testDeserializedSpanContext_emptyTraceState() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceStateString = ""
        )
        assertEquals(0, context.traceState.asMap().size)
    }

    @Test
    fun testDeserializedTraceFlags_sampled() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            flags = 0x01
        )
        assertTrue(context.traceFlags.isSampled)
        assertFalse(context.traceFlags.isRandom)
    }

    @Test
    fun testDeserializedTraceFlags_random() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            flags = 0x02
        )
        assertFalse(context.traceFlags.isSampled)
        assertTrue(context.traceFlags.isRandom)
    }

    @Test
    fun testDeserializedTraceFlags_sampledAndRandom() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            flags = 0x03
        )
        assertTrue(context.traceFlags.isSampled)
        assertTrue(context.traceFlags.isRandom)
    }

    @Test
    fun testDeserializedTraceState_put_throws() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray()
        )
        assertFailsWith<UnsupportedOperationException> {
            context.traceState.put("key", "value")
        }
    }

    @Test
    fun testDeserializedTraceState_remove_throws() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray()
        )
        assertFailsWith<UnsupportedOperationException> {
            context.traceState.remove("key")
        }
    }

    @Test
    fun testTraceStateToW3CString() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceStateString = "foo=bar,baz=qux"
        )
        val w3cString = context.traceState.toW3CString()
        assertTrue(w3cString.contains("foo=bar"))
        assertTrue(w3cString.contains("baz=qux"))
    }
}