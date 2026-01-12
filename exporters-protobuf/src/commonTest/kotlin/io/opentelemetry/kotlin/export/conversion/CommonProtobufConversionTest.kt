package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.resource.FakeResource
import kotlin.collections.get
import kotlin.test.Test
import kotlin.test.assertEquals

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
}