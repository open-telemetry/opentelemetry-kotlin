package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.ArrayValue
import io.opentelemetry.proto.common.v1.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
class AttributeProtobufConversionTest {

    @Test
    fun testEmptyConversion() {
        assertEquals(0, emptyMap<String, Any>().createKeyValues().size)
    }

    @Test
    fun testConversionOfValues() {
        assertEquals(createForValue("string").value_?.string_value, "string")
        assertEquals(createForValue(42L).value_?.int_value, 42L)
        assertEquals(createForValue(0.2).value_?.double_value, 0.2)
        assertEquals(createForValue(true).value_?.bool_value, true)
    }

    @Test
    fun testListConversion() {
        val createKeyValues = mapOf("key" to listOf("val", 32L)).createKeyValues()
        assertEquals(1, createKeyValues.size)
        assertEquals("key", createKeyValues[0].key)
        val array = createKeyValues.first().value_?.array_value?.values
        assertNotNull(array)
        assertEquals(array[0].string_value, "val")
        assertEquals(array[1].int_value, 32L)
    }

    @Test
    fun testUnknownTypeExpectException() {
        assertFailsWith(UnsupportedOperationException::class) {
            createForValue(Any())
        }
    }

    @Test
    fun testAttributeMapDeserialization_string() {
        val keyValues = listOf(KeyValue("key", AnyValue(string_value = "test")))
        val map = keyValues.toAttributeMap()
        assertEquals(1, map.size)
        assertEquals("test", map["key"])
    }

    @Test
    fun testAttributeMapDeserialization_int() {
        val keyValues = listOf(KeyValue("key", AnyValue(int_value = 42L)))
        val map = keyValues.toAttributeMap()
        assertEquals(42L, map["key"])
    }

    @Test
    fun testAttributeMapDeserialization_double() {
        val keyValues = listOf(KeyValue("key", AnyValue(double_value = 3.14)))
        val map = keyValues.toAttributeMap()
        assertEquals(3.14, map["key"])
    }

    @Test
    fun testAttributeMapDeserialization_bool() {
        val keyValues = listOf(KeyValue("key", AnyValue(bool_value = true)))
        val map = keyValues.toAttributeMap()
        assertEquals(true, map["key"])
    }

    @Test
    fun testAttributeMapDeserialization_array() {
        val arrayValue = ArrayValue(listOf(
            AnyValue(string_value = "a"),
            AnyValue(int_value = 1L)
        ))
        val keyValues = listOf(KeyValue("key", AnyValue(array_value = arrayValue)))
        val map = keyValues.toAttributeMap()
        val list = map["key"] as List<*>
        assertEquals(2, list.size)
        assertEquals("a", list[0])
        assertEquals(1L, list[1])
    }

    @Test
    fun testAttributeMapDeserialization_nullValue() {
        val keyValues = listOf(KeyValue("key", null))
        val map = keyValues.toAttributeMap()
        assertEquals(0, map.size)
    }

    @Test
    fun testAttributeMapDeserialization_emptyAnyValue() {
        val keyValues = listOf(KeyValue("key", AnyValue()))
        val map = keyValues.toAttributeMap()
        assertEquals(0, map.size)
    }

    @Test
    fun testAttributeMapDeserialization_mixedTypes() {
        val keyValues = listOf(
            KeyValue("str", AnyValue(string_value = "hello")),
            KeyValue("num", AnyValue(int_value = 100L)),
            KeyValue("flag", AnyValue(bool_value = false))
        )
        val map = keyValues.toAttributeMap()
        assertEquals(3, map.size)
        assertEquals("hello", map["str"])
        assertEquals(100L, map["num"])
        assertEquals(false, map["flag"])
    }

    @Test
    fun testRoundTripAllTypes() {
        val original = mapOf(
            "string" to "value",
            "long" to 123L,
            "double" to 4.56,
            "bool" to true,
            "list" to listOf("a", 1L, 2.0, false)
        )

        val keyValues = original.createKeyValues()
        val deserialized = keyValues.toAttributeMap()

        assertEquals(original["string"], deserialized["string"])
        assertEquals(original["long"], deserialized["long"])
        assertEquals(original["double"], deserialized["double"])
        assertEquals(original["bool"], deserialized["bool"])

        val originalList = original["list"] as List<*>
        val deserializedList = deserialized["list"] as List<*>
        assertEquals(originalList.size, deserializedList.size)
        assertEquals(originalList, deserializedList)
    }

    private fun createForValue(value: Any): KeyValue {
        return mapOf("key" to value).createKeyValues().first()
    }
}