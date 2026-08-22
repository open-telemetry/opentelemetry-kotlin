package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.collections.ReadOnlyMapView

internal class AttributesModel(
    private val attributeLimit: Int = DEFAULT_ATTRIBUTE_LIMIT,
    private val attributeValueLengthLimit: Int = DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT,
    private val attrs: MutableMap<String, Any> = mutableMapOf()
) : AttributesMutator, AttributeContainer {

    internal val readOnlyAttributes: Map<String, Any> = ReadOnlyMapView(attrs)

    /**
     * True when [attributeValueLengthLimit] doesn't truncate anything, which is the default.
     */
    private val truncationDisabled = attributeValueLengthLimit == Int.MAX_VALUE

    override fun setBooleanAttribute(key: String, value: Boolean) {
        ifPreconditionsOk(key) {
            attrs[key] = value
        }
    }

    override fun setStringAttribute(key: String, value: String) {
        ifPreconditionsOk(key) {
            attrs[key] = truncateString(value)
        }
    }

    override fun setLongAttribute(key: String, value: Long) {
        ifPreconditionsOk(key) {
            attrs[key] = value
        }
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        ifPreconditionsOk(key) {
            attrs[key] = value
        }
    }

    override fun setBooleanListAttribute(
        key: String,
        value: List<Boolean>
    ) {
        ifPreconditionsOk(key) {
            attrs[key] = value.toList()
        }
    }

    override fun setStringListAttribute(
        key: String,
        value: List<String>
    ) {
        ifPreconditionsOk(key) {
            attrs[key] = if (truncationDisabled) {
                value.toList()
            } else {
                value.map(::truncateString)
            }
        }
    }

    override fun setLongListAttribute(
        key: String,
        value: List<Long>
    ) {
        ifPreconditionsOk(key) {
            attrs[key] = value.toList()
        }
    }

    override fun setDoubleListAttribute(
        key: String,
        value: List<Double>
    ) {
        ifPreconditionsOk(key) {
            attrs[key] = value.toList()
        }
    }

    override fun setByteArrayAttribute(key: String, value: ByteArray) {
        ifPreconditionsOk(key) {
            attrs[key] = truncateByteArray(value)
        }
    }

    override fun setAnyValueAttribute(key: String, value: AnyValue) {
        ifPreconditionsOk(key) {
            attrs[key] = copyAnyValue(truncateAnyValue(value))
        }
    }

    private fun truncateString(value: String): String =
        if (value.length > attributeValueLengthLimit) {
            value.take(attributeValueLengthLimit)
        } else {
            value
        }

    private fun truncateByteArray(value: ByteArray): ByteArray =
        if (value.size > attributeValueLengthLimit) {
            value.copyOf(attributeValueLengthLimit)
        } else {
            value
        }

    private fun truncateAnyValue(value: AnyValue): AnyValue {
        if (truncationDisabled) {
            return value
        }
        return when (value) {
            is AnyValue.StringValue -> {
                val truncated = truncateString(value.value)
                if (truncated === value.value) {
                    value
                } else {
                    AnyValue.StringValue(truncated)
                }
            }
            is AnyValue.BytesValue -> {
                val truncated = truncateByteArray(value.value)
                if (truncated === value.value) {
                    value
                } else {
                    AnyValue.BytesValue(truncated)
                }
            }
            is AnyValue.ListValue -> {
                var changed = false
                val mapped = value.values.map { item ->
                    val truncated = truncateAnyValue(item)
                    if (truncated !== item) {
                        changed = true
                    }
                    truncated
                }
                if (changed) {
                    AnyValue.ListValue(mapped)
                } else {
                    value
                }
            }
            is AnyValue.MapValue -> {
                var changed = false
                val mapped = value.values.mapValues { entry ->
                    val truncated = truncateAnyValue(entry.value)
                    if (truncated !== entry.value) {
                        changed = true
                    }
                    truncated
                }
                if (changed) {
                    AnyValue.MapValue(mapped)
                } else {
                    value
                }
            }
            AnyValue.NullValue,
            is AnyValue.BoolValue,
            is AnyValue.LongValue,
            is AnyValue.DoubleValue -> {
                value
            }
        }
    }

    private fun copyAnyValue(value: AnyValue): AnyValue = when (value) {
        is AnyValue.BytesValue -> AnyValue.BytesValue(value.value.copyOf())
        is AnyValue.ListValue -> AnyValue.ListValue(value.values.map(::copyAnyValue))
        is AnyValue.MapValue -> AnyValue.MapValue(value.values.mapValues { (_, nested) -> copyAnyValue(nested) })
        AnyValue.NullValue,
        is AnyValue.StringValue,
        is AnyValue.BoolValue,
        is AnyValue.LongValue,
        is AnyValue.DoubleValue -> value
    }

    private var droppedAttributesCountImpl = 0

    /**
     * The number of attributes that were dropped because [attributeLimit] was exceeded.
     */
    val droppedAttributesCount: Int
        get() = droppedAttributesCountImpl

    override val attributes: Map<String, Any>
        get() = attrs.toMap()

    /**
     * Runs [setter] only if [key] passes the attribute preconditions, keeping the drop check and
     * the write coupled so an attribute can never be counted as dropped without also being skipped.
     */
    private fun ifPreconditionsOk(key: String, setter: () -> Unit) {
        if (key.isEmpty()) {
            // Invalid key: ignored, not counted as a dropped attribute.
            return
        }
        if (attrs.contains(key)) {
            // Overwriting an existing attribute never drops.
            setter()
            return
        }
        if (attrs.size >= attributeLimit) {
            droppedAttributesCountImpl++
            return
        }
        setter()
    }

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
