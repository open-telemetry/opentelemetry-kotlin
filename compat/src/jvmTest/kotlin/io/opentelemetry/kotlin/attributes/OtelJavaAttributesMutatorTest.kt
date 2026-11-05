package io.opentelemetry.kotlin.attributes

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.common.Value
import org.junit.Assert.assertEquals
import org.junit.Test

internal class OtelJavaAttributesMutatorTest {

    @Test
    fun testSettersUseTypedKeys() {
        val builder = Attributes.builder()
        OtelJavaAttributesMutator(builder.attributeSink()).apply {
            setStringAttribute("str", "a")
            setBooleanAttribute("bool", true)
            setLongAttribute("long", 1)
            setDoubleAttribute("double", 1.5)
            setStringListAttribute("strList", listOf("a"))
            setBooleanListAttribute("boolList", listOf(true))
            setLongListAttribute("longList", listOf(1L))
            setDoubleListAttribute("doubleList", listOf(1.5))
            setByteArrayAttribute("bytes", byteArrayOf(1, 2))
            setAnyValueAttribute("any", AnyValue.LongValue(3))
        }
        val attrs = builder.build()

        assertEquals("a", attrs.get(AttributeKey.stringKey("str")))
        assertEquals(true, attrs.get(AttributeKey.booleanKey("bool")))
        assertEquals(1L, attrs.get(AttributeKey.longKey("long")))
        assertEquals(1.5, attrs.get(AttributeKey.doubleKey("double")))
        assertEquals(listOf("a"), attrs.get(AttributeKey.stringArrayKey("strList")))
        assertEquals(listOf(true), attrs.get(AttributeKey.booleanArrayKey("boolList")))
        assertEquals(listOf(1L), attrs.get(AttributeKey.longArrayKey("longList")))
        assertEquals(listOf(1.5), attrs.get(AttributeKey.doubleArrayKey("doubleList")))
        assertEquals(Value.of(byteArrayOf(1, 2)), attrs.get(AttributeKey.valueKey("bytes")))
        assertEquals(Value.of(3L), attrs.get(AttributeKey.valueKey("any")))
        assertEquals(10, attrs.size())
    }

    @Test
    fun testListsAreCopied() {
        val builder = Attributes.builder()
        val list = mutableListOf("a")
        OtelJavaAttributesMutator(builder.attributeSink()).setStringListAttribute("key", list)
        list.add("b")

        assertEquals(listOf("a"), builder.build().get(AttributeKey.stringArrayKey("key")))
    }
}
