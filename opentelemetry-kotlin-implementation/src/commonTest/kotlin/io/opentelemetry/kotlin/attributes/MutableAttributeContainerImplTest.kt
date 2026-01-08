package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class MutableAttributeContainerImplTest {

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
        val attrs = MutableAttributeContainerImpl(attributeLimit).apply {
            addTestAttributes()
        }.attributes
        assertEquals(expected, attrs)
    }

    @Test
    fun testAttributesDoNotExceedLimit() {
        val attrs = MutableAttributeContainerImpl(attributeLimit).apply {
            addTestAttributesAlternateValues()
            addTestAttributes("xyz")
            addTestAttributes()
        }.attributes
        assertEquals(expected, attrs)
    }

    private fun MutableAttributeContainer.addTestAttributes(keyToken: String = "") {
        setStringAttribute("string$keyToken", "value")
        setDoubleAttribute("double$keyToken", 3.14)
        setBooleanAttribute("boolean$keyToken", true)
        setLongAttribute("long$keyToken", 90000000000000)
        setStringListAttribute("string_list$keyToken", listOf("string"))
        setDoubleListAttribute("double_list$keyToken", listOf(3.14))
        setBooleanListAttribute("boolean_list$keyToken", listOf(true))
        setLongListAttribute("long_list$keyToken", listOf(90000000000000))
    }

    private fun MutableAttributeContainer.addTestAttributesAlternateValues() {
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
