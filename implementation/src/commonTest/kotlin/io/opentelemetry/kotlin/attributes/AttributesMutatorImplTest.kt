package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class AttributesMutatorImplTest {

    private val attributeLimit = 8
    private val expected = mapOf(
        "string" to "value",
        "double" to 3.14,
        "boolean" to true,
        "long" to 90000000000000,
        "string_list" to listOf("string"),
        "double_list" to listOf(3.14),
        "boolean_list" to listOf(true),
        "long_list" to listOf(90000000000000)
    )

    @Test
    fun testAttributes() {
        val attrs = AttributesModel(attributeLimit).apply {
            addTestAttributes()
        }.attributes
        assertEquals(expected, attrs)
    }

    @Test
    fun testAttributesDoNotExceedLimit() {
        val attrs = AttributesModel(attributeLimit).apply {
            addTestAttributesAlternateValues()
            addTestAttributes("xyz")
            addTestAttributes()
        }.attributes
        assertEquals(expected, attrs)
    }

    @Test
    fun testStringValueTruncated() {
        val attrs = AttributesModel(attributeLimit = attributeLimit, attributeValueLengthLimit = 3).apply {
            setStringAttribute("key", "abcdef")
        }.attributes
        assertEquals("abc", attrs["key"])
    }

    @Test
    fun testStringValueAtLimit() {
        val attrs = AttributesModel(attributeLimit = attributeLimit, attributeValueLengthLimit = 5).apply {
            setStringAttribute("key", "hello")
        }.attributes
        assertEquals("hello", attrs["key"])
    }

    @Test
    fun testStringListValuesTruncated() {
        val attrs = AttributesModel(attributeLimit = attributeLimit, attributeValueLengthLimit = 2).apply {
            setStringListAttribute("key", listOf("hello", "world"))
        }.attributes
        @Suppress("UNCHECKED_CAST")
        assertEquals(listOf("he", "wo"), attrs["key"] as List<String>)
    }

    @Test
    fun testNonStringTypesUnaffected() {
        val attrs = AttributesModel(attributeLimit = attributeLimit, attributeValueLengthLimit = 1).apply {
            setLongAttribute("long", 123456789L)
            setDoubleAttribute("double", 3.14159)
            setBooleanAttribute("bool", true)
            setLongListAttribute("long_list", listOf(1L, 2L, 3L))
            setDoubleListAttribute("double_list", listOf(1.0, 2.0))
            setBooleanListAttribute("bool_list", listOf(true, false))
        }.attributes
        assertEquals(123456789L, attrs["long"])
        assertEquals(3.14159, attrs["double"])
        assertEquals(true, attrs["bool"])
        assertEquals(listOf(1L, 2L, 3L), attrs["long_list"])
        assertEquals(listOf(1.0, 2.0), attrs["double_list"])
        assertEquals(listOf(true, false), attrs["bool_list"])
    }

    @Test
    fun testKeyUpdateAtLimit() {
        val attrs = AttributesModel(attributeLimit = 1, attributeValueLengthLimit = 3).apply {
            setStringAttribute("key", "first")
            // update existing key. should still truncate even though at limit
            setStringAttribute("key", "abcdef")
        }.attributes
        assertEquals("abc", attrs["key"])
    }

    @Test
    fun testByteArrayAttribute() {
        val bytes = byteArrayOf(0x01, 0x02, 0x03)
        val attrs = AttributesModel(attributeLimit).apply {
            setByteArrayAttribute("bytes", bytes)
        }.attributes
        val stored = attrs["bytes"] as ByteArray
        assertTrue(stored.contentEquals(bytes))
    }

    @Test
    fun testByteArrayValueTruncated() {
        val attrs = AttributesModel(attributeLimit = attributeLimit, attributeValueLengthLimit = 3).apply {
            setByteArrayAttribute("bytes", byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06))
        }.attributes
        val stored = attrs["bytes"] as ByteArray
        assertTrue(stored.contentEquals(byteArrayOf(0x01, 0x02, 0x03)))
    }

    @Test
    fun testByteArrayValueAtLimit() {
        val bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
        val attrs = AttributesModel(attributeLimit = attributeLimit, attributeValueLengthLimit = 5).apply {
            setByteArrayAttribute("bytes", bytes)
        }.attributes
        val stored = attrs["bytes"] as ByteArray
        assertTrue(stored.contentEquals(bytes))
    }

    @Test
    fun testEmptyKeyIgnoredForAllTypes() {
        val attrs = AttributesModel(attributeLimit).apply {
            setBooleanAttribute("", true)
            setStringAttribute("", "value")
            setLongAttribute("", 1L)
            setDoubleAttribute("", 1.0)
            setBooleanListAttribute("", listOf(true))
            setStringListAttribute("", listOf("value"))
            setLongListAttribute("", listOf(1L))
            setDoubleListAttribute("", listOf(1.0))
            setByteArrayAttribute("", byteArrayOf(0x01))
            setAnyValueAttribute("", AnyValue.StringValue("value"))
        }.attributes
        assertEquals(emptyMap(), attrs)
    }

    @Test
    fun testAnyValueAttribute() {
        val nested = AnyValue.MapValue(
            mapOf(
                "s" to AnyValue.StringValue("hello"),
                "b" to AnyValue.BoolValue(true),
                "l" to AnyValue.LongValue(42L),
                "d" to AnyValue.DoubleValue(3.14),
                "n" to AnyValue.NullValue,
                "bytes" to AnyValue.BytesValue(byteArrayOf(0x01, 0x02)),
                "list" to AnyValue.ListValue(listOf(AnyValue.StringValue("x")))
            )
        )
        val attrs = AttributesModel(attributeLimit).apply {
            setAnyValueAttribute("any", nested)
        }.attributes
        assertEquals(nested, attrs["any"])
    }

    @Test
    fun testAnyValueEqualityAndHashCodeAcrossInstances() {
        val a = AttributesModel(attributeLimit).apply {
            setAnyValueAttribute("k", AnyValue.MapValue(mapOf("x" to AnyValue.StringValue("v"))))
        }
        val b = AttributesModel(attributeLimit).apply {
            setAnyValueAttribute("k", AnyValue.MapValue(mapOf("x" to AnyValue.StringValue("v"))))
        }
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun testAnyValueStringTruncatedRecursively() {
        val attrs = AttributesModel(
            attributeLimit = attributeLimit,
            attributeValueLengthLimit = 3
        ).apply {
            setAnyValueAttribute(
                "map",
                AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("abcdef")))
            )
            setAnyValueAttribute(
                "list",
                AnyValue.ListValue(listOf(AnyValue.StringValue("abcdef")))
            )
            setAnyValueAttribute("flat", AnyValue.StringValue("abcdef"))
        }.attributes

        val map = attrs["map"] as AnyValue.MapValue
        assertEquals("abc", (map.values["k"] as AnyValue.StringValue).value)
        val list = attrs["list"] as AnyValue.ListValue
        assertEquals("abc", (list.values[0] as AnyValue.StringValue).value)
        assertEquals("abc", (attrs["flat"] as AnyValue.StringValue).value)
    }

    @Test
    fun testAnyValueBytesTruncatedRecursively() {
        val attrs = AttributesModel(
            attributeLimit = attributeLimit,
            attributeValueLengthLimit = 3
        ).apply {
            setAnyValueAttribute(
                "bytes",
                AnyValue.BytesValue(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05))
            )
            setAnyValueAttribute(
                "nested",
                AnyValue.ListValue(
                    listOf(AnyValue.BytesValue(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)))
                )
            )
        }.attributes

        val flat = (attrs["bytes"] as AnyValue.BytesValue).value
        assertTrue(flat.contentEquals(byteArrayOf(0x01, 0x02, 0x03)))
        val nested = ((attrs["nested"] as AnyValue.ListValue).values[0] as AnyValue.BytesValue).value
        assertTrue(nested.contentEquals(byteArrayOf(0x01, 0x02, 0x03)))
    }

    @Test
    fun testAnyValueRespectsAttributeLimit() {
        val model = AttributesModel(attributeLimit = 1).apply {
            setAnyValueAttribute("first", AnyValue.StringValue("a"))
            setAnyValueAttribute("second", AnyValue.StringValue("b"))
        }
        val attrs = model.attributes
        assertEquals(1, attrs.size)
        assertEquals(AnyValue.StringValue("a"), attrs["first"])
    }

    @Test
    fun testEqualityReflexive() {
        val attrs = AttributesModel(attributeLimit).apply { addTestAttributes() }
        assertEquals(attrs, attrs)
    }

    @Test
    fun testEqualityWithSameContent() {
        val a = AttributesModel(attributeLimit).apply { addTestAttributes() }
        val b = AttributesModel(attributeLimit).apply { addTestAttributes() }
        assertEquals(a, b)
    }

    @Test
    fun testEqualityEmptyInstances() {
        assertEquals(AttributesModel(), AttributesModel())
    }

    @Test
    fun testInequalityWithDifferentContent() {
        val a = AttributesModel(attributeLimit).apply { setStringAttribute("key", "a") }
        val b = AttributesModel(attributeLimit).apply { setStringAttribute("key", "b") }
        assertNotEquals(a, b)
    }

    @Test
    fun testInequalityWithDifferentKeys() {
        val a = AttributesModel(attributeLimit).apply { setStringAttribute("key1", "value") }
        val b = AttributesModel(attributeLimit).apply { setStringAttribute("key2", "value") }
        assertNotEquals(a, b)
    }

    @Test
    fun testHashCodeConsistency() {
        val a = AttributesModel(attributeLimit).apply { addTestAttributes() }
        val b = AttributesModel(attributeLimit).apply { addTestAttributes() }
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun testEqualityWithMatchingByteArrayContent() {
        val a = AttributesModel(attributeLimit).apply {
            setByteArrayAttribute("bytes", byteArrayOf(0x01, 0x02, 0x03))
        }
        val b = AttributesModel(attributeLimit).apply {
            setByteArrayAttribute("bytes", byteArrayOf(0x01, 0x02, 0x03))
        }
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun testInequalityWithDifferingByteArrayContent() {
        val a = AttributesModel(attributeLimit).apply {
            setByteArrayAttribute("bytes", byteArrayOf(0x01, 0x02, 0x03))
        }
        val b = AttributesModel(attributeLimit).apply {
            setByteArrayAttribute("bytes", byteArrayOf(0x01, 0x02, 0x04))
        }
        assertNotEquals(a, b)
    }

    private fun AttributesMutator.addTestAttributes(keyToken: String = "") {
        setStringAttribute("string$keyToken", "value")
        setDoubleAttribute("double$keyToken", 3.14)
        setBooleanAttribute("boolean$keyToken", true)
        setLongAttribute("long$keyToken", 90000000000000)
        setStringListAttribute("string_list$keyToken", listOf("string"))
        setDoubleListAttribute("double_list$keyToken", listOf(3.14))
        setBooleanListAttribute("boolean_list$keyToken", listOf(true))
        setLongListAttribute("long_list$keyToken", listOf(90000000000000))
    }

    private fun AttributesMutator.addTestAttributesAlternateValues() {
        setStringAttribute("string", "override")
        setDoubleAttribute("double", 5.4)
        setBooleanAttribute("boolean", false)
        setLongAttribute("long", 80000000000000)
        setStringListAttribute("string_list", listOf("override"))
        setDoubleListAttribute("double_list", listOf(5.4))
        setBooleanListAttribute("boolean_list", listOf(false))
        setLongListAttribute("long_list", listOf(80000000000000))
    }
}
