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

    override val attributes: Map<String, Any>
        get() = attrs.toMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is AttributesModel) {
            return false
        }
        return attrs == other.attrs
    }

    override fun hashCode(): Int = attrs.hashCode()

    private fun canAddAttribute(key: String): Boolean =
        attrs.size < attributeLimit || attrs.contains(key)
}

internal const val DEFAULT_ATTRIBUTE_LIMIT: Int = 128
internal const val DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT: Int = Int.MAX_VALUE
