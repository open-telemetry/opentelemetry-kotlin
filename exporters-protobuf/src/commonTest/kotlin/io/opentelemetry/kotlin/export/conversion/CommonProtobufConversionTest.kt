package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.proto.common.v1.InstrumentationScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

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
    fun testResourceDeserialization_asNewResource() {
        val resource = deserializedResource()

        val newResource = resource.asNewResource {
            attributes["added"] = 5L
            schemaUrl = "https://example.com/schema"
        }

        assertEquals(mapOf<String, Any>("existing" to "value", "added" to 5L), newResource.attributes)
        assertEquals("https://example.com/schema", newResource.schemaUrl)
    }

    @Test
    fun testResourceDeserialization_asNewResourceLeavesOriginalUntouched() {
        val resource = deserializedResource()

        resource.asNewResource {
            attributes.clear()
            attributes["added"] = 5L
            schemaUrl = "https://example.com/schema"
        }

        assertEquals(mapOf<String, Any>("existing" to "value"), resource.attributes)
        assertNull(resource.schemaUrl)
    }

    @Test
    fun testResourceDeserialization_asNewResourceWithoutMutations() {
        val resource = deserializedResource()
        val copy = resource.asNewResource { }
        assertEquals(resource.attributes, copy.attributes)
        assertNull(copy.schemaUrl)
    }

    @Test
    fun testTraceFlagsConversion() {
        val flags = FakeTraceFlags(isSampled = true, isRandom = false)
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
    fun testDeserializedSpanContext_remote() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            flags = 1,
            isRemote = true
        )
        assertTrue(context.isRemote)
        assertTrue(context.traceFlags.isSampled)
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
    fun testDeserializedSpanContext_malformedTraceState() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceStateString = "foo=bar,nokey,baz=qux"
        )
        assertEquals(mapOf("foo" to "bar", "baz" to "qux"), context.traceState.asMap())
    }

    @Test
    fun testDeserializedSpanContext_traceStateWithTrailingSeparator() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceStateString = "foo=bar,"
        )
        assertEquals(mapOf("foo" to "bar"), context.traceState.asMap())
    }

    @Test
    fun testDeserializedSpanContext_traceStateOfOnlySeparators() {
        val context = DeserializedSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceStateString = ",,,"
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
    fun testDeserializedTraceState_put() {
        val state = deserializedTraceState("foo=bar")
        val next = state.put("baz", "qux")
        assertEquals(mapOf("foo" to "bar", "baz" to "qux"), next.asMap())
        assertEquals(mapOf("foo" to "bar"), state.asMap())
    }

    @Test
    fun testDeserializedTraceState_putOverwritesExistingKey() {
        val state = deserializedTraceState("foo=bar")
        assertEquals(mapOf("foo" to "baz"), state.put("foo", "baz").asMap())
    }

    @Test
    fun testDeserializedTraceState_putInvalidKeyReturnsSameInstance() {
        val state = deserializedTraceState("foo=bar")
        assertSame(state, state.put("UPPERCASE", "value"))
        assertSame(state, state.put("", "value"))
        assertSame(state, state.put("too@many@parts", "value"))
    }

    @Test
    fun testDeserializedTraceState_putInvalidValueReturnsSameInstance() {
        val state = deserializedTraceState("foo=bar")
        assertSame(state, state.put("key", ""))
        assertSame(state, state.put("key", "has,comma"))
        assertSame(state, state.put("key", "trailing "))
    }

    @Test
    fun testDeserializedTraceState_putNewKeyAtMaxEntriesReturnsSameInstance() {
        val header = (0 until MAX_TRACE_STATE_ENTRIES).joinToString(",") { "key$it=value$it" }
        val state = deserializedTraceState(header)
        assertEquals(MAX_TRACE_STATE_ENTRIES, state.asMap().size)

        assertSame(state, state.put("extra", "value"))
    }

    @Test
    fun testDeserializedTraceState_putExistingKeyAtMaxEntriesIsAllowed() {
        val header = (0 until MAX_TRACE_STATE_ENTRIES).joinToString(",") { "key$it=value$it" }
        val state = deserializedTraceState(header)

        val next = state.put("key0", "updated")
        assertEquals(MAX_TRACE_STATE_ENTRIES, next.asMap().size)
        assertEquals("updated", next.get("key0"))
    }

    @Test
    fun testDeserializedTraceState_remove() {
        val state = deserializedTraceState("foo=bar,baz=qux")
        val next = state.remove("foo")

        assertEquals(mapOf("baz" to "qux"), next.asMap())

        // the original instance is unchanged
        assertEquals(mapOf("foo" to "bar", "baz" to "qux"), state.asMap())
    }

    @Test
    fun testDeserializedTraceState_removeUnknownKeyReturnsSameInstance() {
        val state = deserializedTraceState("foo=bar")
        assertSame(state, state.remove("unknown"))
    }

    @Test
    fun testDeserializedTraceState_putThenEncode() {
        val state = deserializedTraceState("foo=bar").put("baz", "qux")
        assertEquals("foo=bar,baz=qux", state.toW3CString())
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

    private fun deserializedResource(): Resource {
        val proto = io.opentelemetry.proto.resource.v1.Resource(
            attributes = mapOf<String, Any>("existing" to "value").createKeyValues()
        )
        return proto.toResource()
    }

    private fun deserializedTraceState(header: String): TraceState = DeserializedSpanContext(
        traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
        spanIdBytes = "1234567890123456".hexToByteArray(),
        traceStateString = header,
    ).traceState

    private companion object {
        private const val MAX_TRACE_STATE_ENTRIES = 32
    }
}