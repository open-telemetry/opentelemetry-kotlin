package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.attributes.AnyValue as KotlinAnyValue
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.ArrayValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.common.v1.KeyValueList
import okio.ByteString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        val bytes = byteArrayOf(0x01, 0x02, 0x03)
        assertEquals(createForValue(bytes).value_?.bytes_value, ByteString.of(*bytes))
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
    fun testIntValueDoesNotThrow() {
        val value = 42
        val keyValue = createForValue(value)
        val anyValue = checkNotNull(keyValue.value_)
        val numericValue = anyValue.int_value ?: anyValue.double_value?.toLong()
        assertEquals(42L, numericValue)
    }

    @Test
    fun testFloatValueDoesNotThrow() {
        val keyValue = createForValue(1.5f)
        val anyValue = keyValue.value_
        checkNotNull(anyValue)
        assertEquals(1.5, anyValue.double_value)
    }

    @Test
    fun testShortValueDoesNotThrow() {
        val keyValue = createForValue(1.toShort())
        val anyValue = checkNotNull(keyValue.value_)
        val numericValue = anyValue.int_value ?: anyValue.double_value?.toLong()
        assertEquals(1L, numericValue)
    }

    @Test
    fun testByteValueDoesNotThrow() {
        val keyValue = createForValue(1.toByte())
        val anyValue = checkNotNull(keyValue.value_)
        val numericValue = anyValue.int_value ?: anyValue.double_value?.toLong()
        assertEquals(1L, numericValue)
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
    fun testAttributeMapDeserialization_bytes() {
        val bytes = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
        val keyValues = listOf(KeyValue("key", AnyValue(bytes_value = ByteString.of(*bytes))))
        val map = keyValues.toAttributeMap()
        assertTrue((map["key"] as ByteArray).contentEquals(bytes))
    }

    @Test
    fun testRoundTripByteArray() {
        val value = byteArrayOf(0x01, 0x02, 0x03)
        val original = mapOf("bytes" to value)
        val keyValues = original.createKeyValues()
        val deserialized = keyValues.toAttributeMap()
        assertTrue((deserialized["bytes"] as ByteArray).contentEquals(value))
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

    @Test
    fun testAnyValuePrimitiveConversion() {
        // Previously these threw UnsupportedOperationException, so exporting any span or log
        // record carrying an AnyValue attribute crashed.
        assertEquals(3L, createForValue(KotlinAnyValue.LongValue(3)).value_?.int_value)
        assertEquals("s", createForValue(KotlinAnyValue.StringValue("s")).value_?.string_value)
        assertEquals(true, createForValue(KotlinAnyValue.BoolValue(true)).value_?.bool_value)
        assertEquals(3.14, createForValue(KotlinAnyValue.DoubleValue(3.14)).value_?.double_value)

        val bytes = byteArrayOf(0x01, 0x02)
        assertEquals(
            ByteString.of(*bytes),
            createForValue(KotlinAnyValue.BytesValue(bytes)).value_?.bytes_value
        )
    }

    @Test
    fun testAnyValueNullConversion() {
        val anyValue = checkNotNull(createForValue(KotlinAnyValue.NullValue).value_)
        assertNull(anyValue.string_value)
        assertNull(anyValue.int_value)
        assertNull(anyValue.bool_value)
        assertNull(anyValue.double_value)
    }

    @Test
    fun testAnyValueListConversion() {
        val value = KotlinAnyValue.ListValue(
            listOf(KotlinAnyValue.LongValue(1), KotlinAnyValue.StringValue("a"))
        )
        val array = checkNotNull(createForValue(value).value_?.array_value?.values)
        assertEquals(2, array.size)
        assertEquals(1L, array[0].int_value)
        assertEquals("a", array[1].string_value)
    }

    @Test
    fun testAnyValueMapConversion() {
        val value = KotlinAnyValue.MapValue(
            mapOf(
                "long" to KotlinAnyValue.LongValue(1),
                "nested" to KotlinAnyValue.MapValue(mapOf("s" to KotlinAnyValue.StringValue("a")))
            )
        )
        val kvlist = checkNotNull(createForValue(value).value_?.kvlist_value?.values)
        assertEquals(2, kvlist.size)
        assertEquals("long", kvlist[0].key)
        assertEquals(1L, kvlist[0].value_?.int_value)
        assertEquals("a", kvlist[1].value_?.kvlist_value?.values?.first()?.value_?.string_value)
    }

    @Test
    fun testAttributeMapDeserialization_kvlist() {
        val kvlist = KeyValueList(
            listOf(
                KeyValue("long", AnyValue(int_value = 1L)),
                KeyValue("missing", null)
            )
        )
        val keyValues = listOf(KeyValue("key", AnyValue(kvlist_value = kvlist)))

        val map = keyValues.toAttributeMap()
        assertEquals(
            KotlinAnyValue.MapValue(
                mapOf(
                    "long" to KotlinAnyValue.LongValue(1),
                    "missing" to KotlinAnyValue.NullValue
                )
            ),
            map["key"]
        )
    }

    @Test
    fun testRoundTripAnyValueMap() {
        val value = KotlinAnyValue.MapValue(
            mapOf(
                "long" to KotlinAnyValue.LongValue(3),
                "list" to KotlinAnyValue.ListValue(listOf(KotlinAnyValue.StringValue("a")))
            )
        )
        val deserialized = mapOf("key" to value).createKeyValues().toAttributeMap()
        assertEquals(value, deserialized["key"])
    }

    @Test
    fun testAnyValueNullAttributeIsDropped() {
        // The spec has no null attribute value, so a NullValue serializes to an empty protobuf
        // AnyValue and comes back with the key absent.
        val deserialized = mapOf<String, Any>("key" to KotlinAnyValue.NullValue)
            .createKeyValues()
            .toAttributeMap()
        assertEquals(0, deserialized.size)
    }

    @Test
    fun testRoundTripAnyValuePrimitivesFlattenToPlainValues() {
        // Protobuf cannot distinguish AnyValue.LongValue(3) from a plain Long, so a primitive
        // AnyValue deserializes to the plain Kotlin value. Documenting the asymmetry.
        val deserialized = mapOf<String, Any>("key" to KotlinAnyValue.LongValue(3))
            .createKeyValues()
            .toAttributeMap()
        assertEquals(3L, deserialized["key"])
    }

    private fun createForValue(value: Any): KeyValue {
        return mapOf("key" to value).createKeyValues().first()
    }
}