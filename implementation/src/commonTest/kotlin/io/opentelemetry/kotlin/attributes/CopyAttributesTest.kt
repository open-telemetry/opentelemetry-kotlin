package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CopyAttributesTest {

    @Test
    fun testWholeDoublesPreserved() {
        val source = AttributesModel().apply {
            setDoubleAttribute("double", 2.0)
            setDoubleListAttribute("double_list", listOf(1.0, 2.0))
        }
        val attrs = AttributesModel().apply { copyAttributes(source) }
        assertEquals(source.typedAttributes, attrs.typedAttributes)
        assertEquals(mapOf("double" to 2.0, "double_list" to listOf(1.0, 2.0)), attrs.attributes)
    }

    @Test
    fun testHeterogeneousListIsStringified() {
        val attrs = AttributesModel().apply {
            copyAttributes(untyped(mapOf("mixed" to listOf(1.5, "a"))))
        }.attributes
        assertEquals(mapOf("mixed" to listOf("1.5", "a")), attrs)
    }

    @Test
    fun testEmptyListFromUntypedContainer() {
        val attrs = AttributesModel().apply {
            copyAttributes(untyped(mapOf("empty" to emptyList<Double>())))
        }.attributes
        assertEquals(mapOf("empty" to emptyList<String>()), attrs)
    }

    @Test
    fun testLaterCopyOverwritesEarlier() {
        val attrs = AttributesModel().apply {
            copyAttributes(untyped(mapOf("key" to "first", "other" to true)))
            copyAttributes(untyped(mapOf("key" to "second")))
        }.attributes
        assertEquals(mapOf("key" to "second", "other" to true), attrs)
    }

    private fun untyped(attributes: Map<String, Any>): AttributeContainer = object : AttributeContainer {
        override val attributes: Map<String, Any> = attributes
    }
}
