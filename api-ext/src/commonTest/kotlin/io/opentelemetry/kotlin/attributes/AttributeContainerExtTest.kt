package io.opentelemetry.kotlin.attributes

import kotlin.test.Test
import kotlin.test.assertEquals

internal class AttributeContainerExtTest {

    @Test
    fun testSetAttributes() {
        val attrs = FakeAttributesMutator()
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
        attrs.setAttributes(input)
        val observed = attrs.attributes
        assertEquals("value", observed["string"])
        assertEquals(5L, (observed["long"] as Number).toLong())
        assertEquals(10.0, (observed["double"] as Number).toDouble())
        assertEquals(true, observed["bool"])
        assertEquals(listOf("value"), observed["stringList"] as List<*>)
        assertEquals(listOf(5L), (observed["longList"] as List<*>).map { (it as Number).toLong() })
        assertEquals(listOf(10.0), (observed["doubleList"] as List<*>).map { (it as Number).toDouble() })
        assertEquals(listOf(true), observed["boolList"] as List<*>)
        assertEquals("ComplexObject", observed["complex"])
    }

    @Test
    fun testIntAttributeStorage() {
        val attrs = FakeAttributesMutator()
        val i = 5
        attrs.setAttributes(mapOf("int" to i))
        val value = attrs.attributes["int"] as Number
        assertEquals(i.toLong(), value.toLong())
    }

    @Test
    fun testFloatAttributeStorage() {
        val attrs = FakeAttributesMutator()
        val f = 1.5f
        attrs.setAttributes(mapOf("float" to f))
        val value = attrs.attributes["float"] as Number
        assertEquals(f.toDouble(), value.toDouble())
    }

    @Test
    fun testByteAttributeStorage() {
        val attrs = FakeAttributesMutator()
        val b = 1.toByte()
        attrs.setAttributes(mapOf("byte" to b))
        val value = attrs.attributes["byte"] as Number
        assertEquals(b.toLong(), value.toLong())
    }

    @Test
    fun testShortAttributeStorage() {
        val attrs = FakeAttributesMutator()
        val s = 1.toShort()
        attrs.setAttributes(mapOf("short" to s))
        val value = attrs.attributes["short"] as Number
        assertEquals(s.toLong(), value.toLong())
    }

    private class ComplexObject {
        override fun toString(): String = "ComplexObject"
    }

    private class FakeAttributesMutator(
        val attributes: MutableMap<String, Any> = mutableMapOf()
    ) : AttributesMutator {
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

        override fun setByteArrayAttribute(key: String, value: ByteArray) {
            attributes[key] = value
        }
    }
}
