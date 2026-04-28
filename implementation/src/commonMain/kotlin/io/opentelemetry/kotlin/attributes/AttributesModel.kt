package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.threadSafeMap

@ThreadSafe
internal class AttributesModel(
    private val attributeLimit: Int = DEFAULT_ATTRIBUTE_LIMIT,
    private val attributeValueLengthLimit: Int = DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT,
    private val attrs: MutableMap<String, Any> = threadSafeMap()
) : AttributesMutator, AttributeContainer {

    override fun setBooleanAttribute(key: String, value: Boolean) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        if (canAddAttribute(key)) {
            attrs[key] = value.take(attributeValueLengthLimit)
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value.map { it.take(attributeValueLengthLimit) }
        }
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        if (canAddAttribute(key)) {
            attrs[key] = value
        }
    }

    override val attributes: Map<String, Any>
        get() = attrs.toMap()

    private fun canAddAttribute(key: String): Boolean =
        key.isNotEmpty() && (attrs.size < attributeLimit || attrs.contains(key))

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is AttributesModel) {
            return false
        }
        val otherAttrs = other.attrs
        if (attrs.size != otherAttrs.size) {
            return false
        }
        for ((key, value) in attrs) {
            val otherValue = otherAttrs[key] ?: return false
            if (!attributeValuesEqual(value, otherValue)) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 0
        for ((key, value) in attrs) {
            result += key.hashCode() xor attributeValueHashCode(value)
        }
        return result
    }

    // ByteArray uses identity equality/hashCode, so route through content-aware variants.
    private fun attributeValuesEqual(a: Any, b: Any): Boolean {
        if (a is ByteArray && b is ByteArray) {
            return a.contentEquals(b)
        }
        return a == b
    }

    private fun attributeValueHashCode(value: Any): Int {
        if (value is ByteArray) {
            return value.contentHashCode()
        }
        return value.hashCode()
    }
}

internal const val DEFAULT_ATTRIBUTE_LIMIT: Int = 128
internal const val DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT: Int = Int.MAX_VALUE
internal const val NO_ATTRIBUTE_LIMIT: Int = Int.MAX_VALUE
