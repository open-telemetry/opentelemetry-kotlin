package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.behavior.limitOrUnset

internal class AttributesModel(
    attributeLimit: Int = DEFAULT_ATTRIBUTE_LIMIT,
    attributeValueLengthLimit: Int = DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT,
    private val attrs: MutableMap<String, AttributeValue> = mutableMapOf()
) : AttributesMutator, TypedAttributeContainer {

    private val attributeLimit: Int = limitOrUnset(attributeLimit) ?: DEFAULT_ATTRIBUTE_LIMIT
    private val lengthLimit: Int = limitOrUnset(attributeValueLengthLimit) ?: DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT

    override fun setBooleanAttribute(key: String, value: Boolean) {
        set(key) { AttributeValue.Bool(value) }
    }

    override fun setStringAttribute(key: String, value: String) {
        set(key) { AttributeValue.Str(value, lengthLimit) }
    }

    override fun setLongAttribute(key: String, value: Long) {
        set(key) { AttributeValue.Long(value) }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        set(key) { AttributeValue.Double(value) }
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        set(key) { AttributeValue.BoolList(value) }
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        set(key) { AttributeValue.StrList(value, lengthLimit) }
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        set(key) { AttributeValue.LongList(value) }
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        set(key) { AttributeValue.DoubleList(value) }
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        set(key) { AttributeValue.Value(AnyValue.BytesValue(value), lengthLimit) }
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        set(key) { AttributeValue.Value(value, lengthLimit) }
    }

    override fun removeAttribute(key: String) {
        attrs.remove(key)
    }

    private var droppedAttributesCountImpl = 0

    /**
     * The number of attributes that were dropped because [attributeLimit] was exceeded.
     */
    val droppedAttributesCount: Int
        get() = droppedAttributesCountImpl

    override val typedAttributes: Map<String, AttributeValue>
        get() = attrs.toMap()

    override val attributes: Map<String, Any>
        get() = when {
            attrs.isEmpty() -> emptyMap()
            else -> attrs.mapValues { it.value.toAny() }
        }

    /**
     * Stores the result of [value] only if [key] passes the attribute preconditions, keeping the
     * drop check and the write coupled so an attribute can never be counted as dropped without
     * also being skipped. [value] is only evaluated when the write happens, so dropped values are
     * never copied.
     */
    private inline fun set(key: String, value: () -> AttributeValue) {
        if (key.isEmpty()) {
            return
        }
        if (!attrs.contains(key) && attrs.size >= attributeLimit) {
            droppedAttributesCountImpl++
            return
        }
        attrs[key] = value()
    }

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
}

internal const val DEFAULT_ATTRIBUTE_LIMIT: Int = 128
internal const val DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT: Int = Int.MAX_VALUE
internal const val NO_ATTRIBUTE_LIMIT: Int = Int.MAX_VALUE
