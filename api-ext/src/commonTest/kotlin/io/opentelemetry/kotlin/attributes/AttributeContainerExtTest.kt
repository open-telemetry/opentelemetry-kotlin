package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class AttributeContainerExtTest {

    @Test
    fun testSetAttributes() {
        val attrs = FakeMutableAttributeContainer()
        val input = mapOf(
            "string" to "value",
            "long" to 5L,
            "double" to 10.0,
            "bool" to true,
            "stringList" to listOf("value"),
            "longList" to mutableListOf(5L),
            "doubleList" to setOf(10.0),
            "boolList" to arrayOf(true),
            "complex" to ComplexObject(),
        )
        val expected = mapOf(
            "string" to "value",
            "long" to 5L,
            "double" to 10.0,
            "bool" to true,
            "stringList" to listOf("value"),
            "longList" to listOf(5L),
            "doubleList" to listOf(10.0),
            "boolList" to listOf(true),
            "complex" to "ComplexObject"
        )
        attrs.setAttributes(input)
        val observed = attrs.attributes
        assertEquals(expected, observed)
    }

    private class ComplexObject {
        override fun toString(): String = "ComplexObject"
    }

    private class FakeMutableAttributeContainer(
        override val attributes: MutableMap<String, Any> = mutableMapOf()
    ) : MutableAttributeContainer {
        override fun setBooleanAttribute(key: String, value: Boolean) {
            attributes[key] = value
        }

        override fun setStringAttribute(key: String, value: String) {
            attributes[key] = value
        }

        override fun setLongAttribute(key: String, value: Long) {
            attributes[key] = value
        }

        override fun setDoubleAttribute(key: String, value: Double) {
            attributes[key] = value
        }

        override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
            attributes[key] = value
        }

        override fun setStringListAttribute(key: String, value: List<String>) {
            attributes[key] = value
        }

        override fun setLongListAttribute(key: String, value: List<Long>) {
            attributes[key] = value
        }

        override fun setDoubleListAttribute(key: String, value: List<Double>) {
            attributes[key] = value
        }
    }
}
