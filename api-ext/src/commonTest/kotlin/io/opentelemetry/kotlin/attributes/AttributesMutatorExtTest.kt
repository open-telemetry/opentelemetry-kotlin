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
            listOf("TestObj(first=one, second=1)", "TestObj(first=two, second=2)"),
            mutator.attributes["arrayobj"] as List<*>
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
    fun `collections containing nulls are stringified`() {
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
        assertEquals(listOf("a", "null"), observed["stringsAndNull"])
        assertEquals(listOf("1", "null"), observed["longsAndNull"])
        assertEquals(listOf("null", "null"), observed["onlyNulls"])
        assertEquals(listOf("a", "null"), observed["arrayWithNull"])
    }

    @Test
    fun `heterogeneous collections are stringified`() {
        val mutator = FakeAttributesMutator()
        mutator.setAttributes(mapOf("mixed" to listOf("a", 1L, true)))
        assertEquals(listOf("a", "1", "true"), mutator.attributes["mixed"])
    }

    private data class TestObj(val first: String, val second: Long)

    private class ComplexObject {
        override fun toString(): String = "ComplexObject"
    }
}
