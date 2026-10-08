package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CopyTypedAttributesTest {

    @Test
    fun testWholeDoublesPreserved() {
        val attrs = AttributesModel().apply {
            copyTypedAttributes(mapOf("double" to 2.0, "double_list" to listOf(1.0, 2.0)))
        }.attributes
        assertEquals(mapOf("double" to 2.0, "double_list" to listOf(1.0, 2.0)), attrs)
    }

    @Test
    fun testHeterogeneousListStartingWithDoubleIsStringified() {
        val attrs = AttributesModel().apply {
            copyTypedAttributes(mapOf("mixed" to listOf(1.5, "a")))
        }.attributes
        assertEquals(mapOf("mixed" to listOf("1.5", "a")), attrs)
    }

    @Test
    fun testEmptyListDelegatesToSetAttributes() {
        val attrs = AttributesModel().apply {
            copyTypedAttributes(mapOf("empty" to emptyList<Double>()))
        }.attributes
        assertEquals(mapOf("empty" to emptyList<String>()), attrs)
    }
}
