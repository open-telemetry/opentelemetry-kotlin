package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals

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
