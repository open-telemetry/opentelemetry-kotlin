package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TypedAttributesTest {

    @Test
    fun emptyJavaAttributeArraysKeepTheirTypes() {
        val attributes = OtelJavaAttributes.builder()
            .put(OtelJavaAttributeKey.booleanArrayKey("booleans"), emptyList())
            .put(OtelJavaAttributeKey.stringArrayKey("strings"), emptyList())
            .put(OtelJavaAttributeKey.longArrayKey("longs"), emptyList())
            .put(OtelJavaAttributeKey.doubleArrayKey("doubles"), emptyList())
            .build()
        val mutator = RecordingAttributesMutator()

        mutator.setTypedAttributes(attributes)

        assertEquals(
            mapOf(
                "booleans" to AttributeSetter.BOOLEAN_LIST,
                "strings" to AttributeSetter.STRING_LIST,
                "longs" to AttributeSetter.LONG_LIST,
                "doubles" to AttributeSetter.DOUBLE_LIST,
            ),
            mutator.setters,
        )
    }

    private enum class AttributeSetter {
        BOOLEAN,
        STRING,
        LONG,
        DOUBLE,
        BOOLEAN_LIST,
        STRING_LIST,
        LONG_LIST,
        DOUBLE_LIST,
        BYTE_ARRAY,
        ANY_VALUE,
    }

    private class RecordingAttributesMutator : AttributesMutator {
        val setters = mutableMapOf<String, AttributeSetter>()

        override fun setBooleanAttribute(key: String, value: Boolean) {
            setters[key] = AttributeSetter.BOOLEAN
        }

        override fun setStringAttribute(key: String, value: String) {
            setters[key] = AttributeSetter.STRING
        }

        override fun setLongAttribute(key: String, value: Long) {
            setters[key] = AttributeSetter.LONG
        }

        override fun setDoubleAttribute(key: String, value: Double) {
            setters[key] = AttributeSetter.DOUBLE
        }

        override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
            setters[key] = AttributeSetter.BOOLEAN_LIST
        }

        override fun setStringListAttribute(key: String, value: List<String>) {
            setters[key] = AttributeSetter.STRING_LIST
        }

        override fun setLongListAttribute(key: String, value: List<Long>) {
            setters[key] = AttributeSetter.LONG_LIST
        }

        override fun setDoubleListAttribute(key: String, value: List<Double>) {
            setters[key] = AttributeSetter.DOUBLE_LIST
        }

        override fun setByteArrayAttribute(key: String, value: ByteArray) {
            setters[key] = AttributeSetter.BYTE_ARRAY
        }

        override fun setAnyValueAttribute(key: String, value: AnyValue) {
            setters[key] = AttributeSetter.ANY_VALUE
        }
    }
}
