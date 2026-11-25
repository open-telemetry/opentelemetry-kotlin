package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AttributesMutatorExtTest {

    @Test
    fun `can set attributes with map`() {
        val mutator = FakeAttributesMutator()
        mutator.setLongAttribute("existing", 55.44.toLong())
        val anyValue = AnyValue.MapValue(mapOf("nested" to AnyValue.StringValue("v")))
        val map = mapOf(
            Pair("foo", "bar"), Pair("long", 21L),
            Pair("int", 123), Pair("double", 21.5), Pair("float", 22.5f),
            Pair("byte", 0x7F.toByte()), Pair("bool", true),
            Pair("bytearray", byteArrayOf(1, 2, 3)),
            Pair("list", listOf("foo", "bar", "baz")),
            Pair("tostring", TestObj("flim", 66L)),
            Pair("arrayobj", arrayOf(TestObj("one", 1), TestObj("two", 2))),
            Pair("anyvalue", anyValue)
        )
        mutator.setAttributes(map)

        assertEquals("bar", mutator.attributes["foo"])
        assertEquals(21L, mutator.attributes["long"])
        assertEquals(123L, mutator.attributes["int"])
        assertEquals(21.5, mutator.attributes["double"])
        assertEquals(22.5, mutator.attributes["float"])
        assertEquals(127L, mutator.attributes["byte"])
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(mutator.attributes["bytearray"] as ByteArray?))
        assertEquals(listOf("foo", "bar", "baz"), mutator.attributes["list"])
        assertEquals("TestObj(first=flim, second=66)", mutator.attributes["tostring"])
        assertEquals(
            AnyValue.ListValue(
                listOf(
                    AnyValue.StringValue("TestObj(first=one, second=1)"),
                    AnyValue.StringValue("TestObj(first=two, second=2)")
                )
            ),
            mutator.attributes["arrayobj"]
        )
        assertEquals(55.44.toLong(), mutator.attributes["existing"])
        assertEquals(anyValue, mutator.attributes["anyvalue"])
    }

    @Test
    fun `can set attributes from collections of every supported type`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(
            mapOf(
                "string" to "value",
                "long" to 5L,
                "double" to 10.5,
                "bool" to true,
                "stringList" to listOf("value"),
                "longList" to mutableListOf(5L),
                "doubleList" to setOf(10.5),
                "boolList" to arrayOf(true),
                "complex" to ComplexObject(),
            )
        )
        val observed = mutator.attributes
        assertEquals("value", observed["string"])
        assertEquals(5L, observed["long"])
        assertEquals(10.5, observed["double"])
        assertEquals(true, observed["bool"])
        assertEquals(listOf("value"), observed["stringList"])
        assertEquals(listOf(5L), observed["longList"])
        assertEquals(listOf(10.5), observed["doubleList"])
        assertEquals(listOf(true), observed["boolList"])
        assertEquals("ComplexObject", observed["complex"])
    }

    @Test
    fun testAnyValueAttributeStorage() {
        val mutator = FakeAttributesMutator()
        val anyValue = AnyValue.MapValue(mapOf("nested" to AnyValue.StringValue("v")))
        mutator.setAttributes(mapOf("any" to anyValue))
        assertEquals(anyValue, mutator.attributes["any"])
    }

    @Test
    fun `narrower numbers are widened`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(
            mapOf(
                "int" to 5,
                "short" to 1.toShort(),
                "byte" to 1.toByte(),
                "float" to 1.5f,
                "intList" to listOf(1, 2),
                "floatList" to listOf(1.5f),
            )
        )
        val observed = mutator.attributes
        assertEquals(5L, observed["int"])
        assertEquals(1L, observed["short"])
        assertEquals(1L, observed["byte"])
        assertEquals(1.5, observed["float"])
        assertEquals(listOf(1L, 2L), observed["intList"])
        assertEquals(listOf(1.5), observed["floatList"])
    }

    @Test
    fun `whole numbers are widened to longs`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(
            mapOf(
                "double" to 42.0,
                "float" to 42.0f,
                "doubleList" to listOf(1.0, 2.0),
            )
        )
        assertEquals(42L, mutator.attributes["double"])
        assertEquals(42L, mutator.attributes["float"])
        assertEquals(listOf(1L, 2L), mutator.attributes["doubleList"])
    }

    @Test
    fun `byte arrays are set rather than dropped`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(mapOf("bytes" to byteArrayOf(1, 2)))
        assertTrue(byteArrayOf(1, 2).contentEquals(mutator.attributes["bytes"] as ByteArray?))
    }

    @Test
    fun `empty collections are set rather than dropped`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(
            mapOf(
                "emptyList" to emptyList<String>(),
                "emptySet" to emptySet<Long>(),
                "emptyArray" to emptyArray<Any>(),
            )
        )
        val observed = mutator.attributes
        assertEquals(emptyList<String>(), observed["emptyList"])
        assertEquals(emptyList<String>(), observed["emptySet"])
        assertEquals(emptyList<String>(), observed["emptyArray"])
    }

    @Test
    fun `collections containing nulls become AnyValue ListValue with NullValue`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(
            mapOf(
                "stringsAndNull" to listOf("a", null),
                "longsAndNull" to listOf(1L, null),
                "onlyNulls" to listOf(null, null),
                "arrayWithNull" to arrayOf("a", null),
            )
        )
        val observed = mutator.attributes
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.NullValue)),
            observed["stringsAndNull"]
        )
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.LongValue(1L), AnyValue.NullValue)),
            observed["longsAndNull"]
        )
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.NullValue, AnyValue.NullValue)),
            observed["onlyNulls"]
        )
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.NullValue)),
            observed["arrayWithNull"]
        )
    }

    @Test
    fun `heterogeneous collections become AnyValue ListValue`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(mapOf("mixed" to listOf("a", 1L, true)))
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("a"), AnyValue.LongValue(1L), AnyValue.BoolValue(true))),
            mutator.attributes["mixed"]
        )
    }

    @Test
    fun `primitive arrays become matching typed list`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(
            mapOf(
                "booleans" to booleanArrayOf(true, false),
                "longs" to longArrayOf(10L, 20L),
                "ints" to intArrayOf(1, 2),
                "shorts" to shortArrayOf(3.toShort(), 4.toShort()),
                "doubles" to doubleArrayOf(1.5, 2.5),
                "floats" to floatArrayOf(3.5f, 4.5f),
                "chars" to charArrayOf('x', 'y'),
            )
        )
        val observed = mutator.attributes
        assertEquals(listOf(true, false), observed["booleans"])
        assertEquals(listOf(10L, 20L), observed["longs"])
        assertEquals(listOf(1L, 2L), observed["ints"])
        assertEquals(listOf(3L, 4L), observed["shorts"])
        assertEquals(listOf(1.5, 2.5), observed["doubles"])
        assertEquals(listOf(3.5, 4.5), observed["floats"])
        assertEquals(listOf("x", "y"), observed["chars"])
    }

    @Test
    fun `maps become AnyValue MapValue with keys converted via toString`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(
            mapOf(
                "simpleMap" to mapOf("k" to "v", "num" to 42L),
                "nonStringKeyMap" to mapOf(123 to "val", true to false),
                "nestedMap" to mapOf("inner" to mapOf("a" to 1L)),
                "mapWithNull" to mapOf("nullKey" to null),
            )
        )
        val observed = mutator.attributes
        assertEquals(
            AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("v"), "num" to AnyValue.LongValue(42L))),
            observed["simpleMap"]
        )
        assertEquals(
            AnyValue.MapValue(mapOf("123" to AnyValue.StringValue("val"), "true" to AnyValue.BoolValue(false))),
            observed["nonStringKeyMap"]
        )
        assertEquals(
            AnyValue.MapValue(mapOf("inner" to AnyValue.MapValue(mapOf("a" to AnyValue.LongValue(1L))))),
            observed["nestedMap"]
        )
        assertEquals(
            AnyValue.MapValue(mapOf("nullKey" to AnyValue.NullValue)),
            observed["mapWithNull"]
        )
    }

    @Test
    fun `byteArray elements inside lists become BytesValue`() {
        val mutator = FakeAttributesMutator()
        val bytes1 = byteArrayOf(1, 2)
        val bytes2 = byteArrayOf(3, 4)
        mutator.setAttributes(
            mapOf(
                "byteArrayList" to listOf(bytes1, bytes2),
                "mixedWithBytes" to listOf("text", bytes1, null),
            )
        )
        val observed = mutator.attributes
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.BytesValue(bytes1), AnyValue.BytesValue(bytes2))),
            observed["byteArrayList"]
        )
        assertEquals(
            AnyValue.ListValue(listOf(AnyValue.StringValue("text"), AnyValue.BytesValue(bytes1), AnyValue.NullValue)),
            observed["mixedWithBytes"]
        )
    }

    private data class TestObj(val first: String, val second: Long)

    private class ComplexObject {
        override fun toString(): String = "ComplexObject"
    }
}
