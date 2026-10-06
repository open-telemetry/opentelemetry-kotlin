package io.opentelemetry.kotlin.attributes

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Value
import io.opentelemetry.kotlin.resource.FakeResource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

internal class OtelJavaAttributesExtTest {

    @Test
    fun testAttrsFromMapAnyValueLong() {
        val attrs = attrsFromMap(mapOf("key" to AnyValue.LongValue(3)))

        // The value must be flattened to the long 3, not serialized via AnyValue.toString().
        assertEquals(3L, attrs.get(AttributeKey.longKey("key")))
        assertNotEquals("LongValue(value=3)", attrs.get(AttributeKey.stringKey("key")))
        assertNull(attrs.get(AttributeKey.stringKey("key")))
    }

    @Test
    fun testAttrsFromMapFlattensPrimitiveAnyValues() {
        val attrs = attrsFromMap(
            mapOf(
                "str" to AnyValue.StringValue("hello"),
                "bool" to AnyValue.BoolValue(true),
                "long" to AnyValue.LongValue(42),
                "double" to AnyValue.DoubleValue(3.14)
            )
        )
        assertEquals("hello", attrs.get(AttributeKey.stringKey("str")))
        assertEquals(true, attrs.get(AttributeKey.booleanKey("bool")))
        assertEquals(42L, attrs.get(AttributeKey.longKey("long")))
        assertEquals(3.14, attrs.get(AttributeKey.doubleKey("double")))
    }

    @Test
    fun testAttrsFromMapStoresComplexAnyValues() {
        val attrs = attrsFromMap(
            mapOf(
                "null" to AnyValue.NullValue,
                "bytes" to AnyValue.BytesValue(byteArrayOf(1, 2)),
                "list" to AnyValue.ListValue(listOf(AnyValue.LongValue(1))),
                "map" to AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("v")))
            )
        )
        assertEquals(Value.empty(), attrs.get(AttributeKey.valueKey("null")))
        assertEquals(Value.of(byteArrayOf(1, 2)), attrs.get(AttributeKey.valueKey("bytes")))
        assertEquals(Value.of(listOf(Value.of(1L))), attrs.get(AttributeKey.valueKey("list")))
        assertEquals(Value.of(mapOf("k" to Value.of("v"))), attrs.get(AttributeKey.valueKey("map")))
    }

    @Test
    fun testAttrsFromMapStoresByteArray() {
        val attrs = attrsFromMap(mapOf("bytes" to byteArrayOf(1, 2)))
        assertEquals(Value.of(byteArrayOf(1, 2)), attrs.get(AttributeKey.valueKey("bytes")))
    }

    @Test
    fun testComplexAnyValuesRoundTripViaConvertToMap() {
        val original = mapOf<String, Any>(
            "null" to AnyValue.NullValue,
            "list" to AnyValue.ListValue(listOf(AnyValue.LongValue(1), AnyValue.StringValue("a"))),
            "map" to AnyValue.MapValue(
                mapOf("nested" to AnyValue.ListValue(listOf(AnyValue.BoolValue(true))))
            )
        )
        assertEquals(original, attrsFromMap(original).convertToMap())
    }

    @Test
    fun testBytesRoundTripViaConvertToMap() {
        val map = attrsFromMap(mapOf("bytes" to byteArrayOf(1, 2))).convertToMap()
        assertEquals(AnyValue.BytesValue(byteArrayOf(1, 2)), map["bytes"])
    }

    @Test
    fun testAttrsFromMapPreservesTypes() {
        val attrs = attrsFromMap(
            mapOf(
                "str" to "hello",
                "long" to 42L,
                "double" to 3.14,
                "wholeDouble" to 42.0,
                "bool" to true,
                "strList" to listOf("a", "b"),
                "longList" to listOf(1L, 2L),
                "doubleList" to listOf(1.0, 2.0),
                "boolList" to listOf(true, false)
            )
        )
        assertEquals("hello", attrs.get(AttributeKey.stringKey("str")))
        assertEquals(42L, attrs.get(AttributeKey.longKey("long")))
        assertEquals(3.14, attrs.get(AttributeKey.doubleKey("double")))
        // A whole-valued double must stay a double rather than collapsing to a long.
        assertEquals(42.0, attrs.get(AttributeKey.doubleKey("wholeDouble")))
        assertEquals(true, attrs.get(AttributeKey.booleanKey("bool")))
        assertEquals(listOf("a", "b"), attrs.get(AttributeKey.stringArrayKey("strList")))
        assertEquals(listOf(1L, 2L), attrs.get(AttributeKey.longArrayKey("longList")))
        assertEquals(listOf(1.0, 2.0), attrs.get(AttributeKey.doubleArrayKey("doubleList")))
        assertEquals(listOf(true, false), attrs.get(AttributeKey.booleanArrayKey("boolList")))
    }

    @Test
    fun testAttrsFromMapWidensNarrowerNumbers() {
        val attrs = attrsFromMap(
            mapOf(
                "int" to 42,
                "short" to 1.toShort(),
                "byte" to 0x7F.toByte(),
                "float" to 1.5f,
                "intList" to listOf(1, 2),
                "floatList" to listOf(1.5f)
            )
        )
        assertEquals(42L, attrs.get(AttributeKey.longKey("int")))
        assertEquals(1L, attrs.get(AttributeKey.longKey("short")))
        assertEquals(127L, attrs.get(AttributeKey.longKey("byte")))
        assertEquals(1.5, attrs.get(AttributeKey.doubleKey("float")))
        assertEquals(listOf(1L, 2L), attrs.get(AttributeKey.longArrayKey("intList")))
        assertEquals(listOf(1.5), attrs.get(AttributeKey.doubleArrayKey("floatList")))
    }

    @Test
    fun testAttrsFromMapStringifiesUnsupportedValues() {
        val attrs = attrsFromMap(
            mapOf(
                "mixedList" to listOf("a", 1L),
                "emptyList" to emptyList<String>(),
                "object" to Any0
            )
        )
        // A heterogeneous list has no typed equivalent, so each element is stringified.
        assertEquals(listOf("a", "1"), attrs.get(AttributeKey.stringArrayKey("mixedList")))
        assertEquals(emptyList<String>(), attrs.get(AttributeKey.stringArrayKey("emptyList")))
        assertEquals("Any0", attrs.get(AttributeKey.stringKey("object")))
    }

    private object Any0 {
        override fun toString(): String = "Any0"
    }

    @Test
    fun testAttrsFromMapRoundTripsViaConvertToMap() {
        val original = mapOf<String, Any>(
            "str" to "hello",
            "long" to 42L,
            "double" to 42.0,
            "bool" to true,
            "doubleList" to listOf(1.0, 2.0)
        )
        assertEquals(original, attrsFromMap(original).convertToMap())
    }

    @Test
    fun testResourceFromMap() {
        val resource = resourceFromMap(
            FakeResource(
                attributes = mapOf("long" to 42L, "any" to AnyValue.StringValue("hello")),
                schemaUrl = "https://example.com/schema"
            )
        )
        assertEquals(42L, resource.attributes.get(AttributeKey.longKey("long")))
        assertEquals("hello", resource.attributes.get(AttributeKey.stringKey("any")))
        assertEquals("https://example.com/schema", resource.schemaUrl)
    }

    @Test
    fun testResourceFromMapReusesConversionForSameInstance() {
        val resource = FakeResource()
        assertSame(resourceFromMap(resource), resourceFromMap(resource))
    }

    @Test
    fun testResourceFromMapConvertsDifferentInstances() {
        val first = FakeResource(attributes = mapOf("k" to "first"))
        val second = FakeResource(attributes = mapOf("k" to "second"))
        assertEquals("first", resourceFromMap(first).attributes.get(AttributeKey.stringKey("k")))
        assertEquals("second", resourceFromMap(second).attributes.get(AttributeKey.stringKey("k")))
        assertEquals("first", resourceFromMap(first).attributes.get(AttributeKey.stringKey("k")))
    }

    @Test
    fun testGetOtelJavaAttribute() {
        val attrs = mapOf("str" to "value", "long" to 5L, "list" to listOf("a", "b"))
        assertEquals("value", attrs.getOtelJavaAttribute(AttributeKey.stringKey("str")))
        assertEquals(5L, attrs.getOtelJavaAttribute(AttributeKey.longKey("long")))
        assertEquals(listOf("a", "b"), attrs.getOtelJavaAttribute(AttributeKey.stringArrayKey("list")))
        assertNull(attrs.getOtelJavaAttribute(AttributeKey.stringKey("long")))
        assertNull(attrs.getOtelJavaAttribute(AttributeKey.stringKey("missing")))
    }
}
