package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ThreadSafe

/**
 * Sets attributes on an [io.opentelemetry.kotlin.attributes.AttributesMutator] from a [Map],
 * dispatching each value to the setter that matches its runtime type.
 *
 * Values of a type that the OpenTelemetry API cannot represent are recorded as their [toString]
 * rather than dropped. In particular:
 *
 * - a heterogeneous collection is recorded as a list of strings, where a `null` element renders
 *   as `"null"`
 * - an empty collection is recorded as an empty list of strings
 * - a [Number] other than a [Long] is widened to a long when its value is whole, and to a double
 *   otherwise.
 *
 * https://opentelemetry.io/docs/specs/otel/common/#attribute
 */
@ThreadSafe
public fun AttributesMutator.setAttributes(attributes: Map<String, Any>) {
    attributes.forEach {
        when (val input = it.value) {
            is AnyValue -> setAnyValueAttribute(it.key, input)
            is String -> setStringAttribute(it.key, input)
            is Boolean -> setBooleanAttribute(it.key, input)
            is Long -> setLongAttribute(it.key, input)
            is Number -> setNumericAttribute(it.key, input)
            is ByteArray -> setByteArrayAttribute(it.key, input)
            is Collection<*> -> handleCollection(it.key, input.toList())
            is Array<*> -> handleCollection(it.key, input.toList())
            else -> setStringAttribute(it.key, input.toString())
        }
    }
}

private fun AttributesMutator.setNumericAttribute(key: String, value: Number) {
    if (value.isWholeNumber()) {
        setLongAttribute(key, value.toLong())
    } else {
        setDoubleAttribute(key, value.toDouble())
    }
}

@Suppress("UNCHECKED_CAST")
private fun AttributesMutator.handleCollection(key: String, input: List<*>) {
    when {
        input.all { it is String } -> setStringListAttribute(key, input as List<String>)
        input.all { it is Boolean } -> setBooleanListAttribute(key, input as List<Boolean>)
        input.all { it is Long } -> setLongListAttribute(key, input as List<Long>)
        input.all { it is Number } -> setNumericListAttribute(
            key,
            input.filterIsInstance<Number>()
        )
        // stringify null elements if the list is heterogeneous
        else -> setStringListAttribute(key, input.map { it.toString() })
    }
}

private fun AttributesMutator.setNumericListAttribute(key: String, values: List<Number>) {
    if (values.all { it.isWholeNumber() }) {
        setLongListAttribute(key, values.map { it.toLong() })
    } else {
        setDoubleListAttribute(key, values.map { it.toDouble() })
    }
}

private fun Number.isWholeNumber(): Boolean {
    val doubleValue = toDouble()
    return doubleValue.isFinite() && doubleValue == toLong().toDouble()
}
