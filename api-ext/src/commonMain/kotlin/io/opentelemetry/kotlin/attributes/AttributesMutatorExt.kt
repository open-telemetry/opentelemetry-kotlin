package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ThreadSafe

/**
 * Sets attributes on an [io.opentelemetry.kotlin.attributes.AttributesMutator] from a [Map],
 * dispatching each value to the setter that matches its runtime type.
 *
 * Values of a type that the OpenTelemetry API cannot represent are recorded as their [toString]
 * rather than dropped. In particular:
 *
 * - a collection containing nulls or mixed types is recorded as an [AnyValue.ListValue], with
 *   [AnyValue.NullValue] for each null
 * - an empty collection is recorded as an empty list of strings
 * - a [Map] is recorded as an [AnyValue.MapValue], with keys converted via [toString]
 * - primitive arrays become the matching typed list
 * - [ByteArray] elements inside lists become [AnyValue.BytesValue]
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
            is BooleanArray -> setBooleanListAttribute(it.key, input.toList())
            is LongArray -> setLongListAttribute(it.key, input.toList())
            is IntArray -> setLongListAttribute(it.key, input.map { n -> n.toLong() })
            is ShortArray -> setLongListAttribute(it.key, input.map { n -> n.toLong() })
            is DoubleArray -> setDoubleListAttribute(it.key, input.toList())
            is FloatArray -> setDoubleListAttribute(it.key, input.map { n -> n.toDouble() })
            is CharArray -> setStringListAttribute(it.key, input.map { c -> c.toString() })
            is Map<*, *> -> setAnyValueAttribute(it.key, input.toAnyValueMap())
            is Collection<*> -> handleCollection(it.key, input, input.toList())
            is Array<*> -> handleCollection(it.key, input, input.toList())
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
private fun AttributesMutator.handleCollection(key: String, root: Any, input: List<*>) {
    when {
        input.isEmpty() -> setStringListAttribute(key, emptyList())
        input.any { it == null } -> setAnyValueAttribute(
            key,
            AnyValue.ListValue(input.map { it.toAnyValue(listOf(root)) })
        )
        input.all { it is String } -> setStringListAttribute(key, input as List<String>)
        input.all { it is Boolean } -> setBooleanListAttribute(key, input as List<Boolean>)
        input.all { it is Long } -> setLongListAttribute(key, input as List<Long>)
        input.all { it is Number } -> setNumericListAttribute(
            key,
            input.filterIsInstance<Number>()
        )
        else -> setAnyValueAttribute(
            key,
            AnyValue.ListValue(input.map { it.toAnyValue(listOf(root)) })
        )
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

private const val MAX_DEPTH = 16

private fun Any?.toAnyValue(ancestors: List<Any> = emptyList()): AnyValue {
    if (this == null || ancestors.size >= MAX_DEPTH || ancestors.any { it === this }) {
        return AnyValue.NullValue
    }
    val path = ancestors + this
    return when (this) {
        is AnyValue -> this
        is String -> AnyValue.StringValue(this)
        is Boolean -> AnyValue.BoolValue(this)
        is Long -> AnyValue.LongValue(this)
        is Double -> AnyValue.DoubleValue(this)
        is Number -> when {
            isWholeNumber() -> AnyValue.LongValue(toLong())
            else -> AnyValue.DoubleValue(toDouble())
        }
        is ByteArray -> AnyValue.BytesValue(copyOf())
        is BooleanArray -> AnyValue.ListValue(map { AnyValue.BoolValue(it) })
        is LongArray -> AnyValue.ListValue(map { AnyValue.LongValue(it) })
        is IntArray -> AnyValue.ListValue(map { AnyValue.LongValue(it.toLong()) })
        is ShortArray -> AnyValue.ListValue(map { AnyValue.LongValue(it.toLong()) })
        is DoubleArray -> AnyValue.ListValue(map { AnyValue.DoubleValue(it) })
        is FloatArray -> AnyValue.ListValue(map { AnyValue.DoubleValue(it.toDouble()) })
        is CharArray -> AnyValue.ListValue(map { AnyValue.StringValue(it.toString()) })
        is Map<*, *> -> toAnyValueMap(ancestors)
        is Collection<*> -> AnyValue.ListValue(map { it.toAnyValue(path) })
        is Array<*> -> AnyValue.ListValue(map { it.toAnyValue(path) })
        else -> AnyValue.StringValue(toString())
    }
}

private fun Map<*, *>.toAnyValueMap(ancestors: List<Any> = emptyList()): AnyValue.MapValue {
    val path = ancestors + this
    val result = LinkedHashMap<String, AnyValue>(size)
    val nextSuffix = mutableMapOf<String, Int>()
    for ((key, value) in this) {
        val baseKey = key.toString()
        var candidateKey = baseKey
        var counter = nextSuffix[baseKey] ?: 1
        while (result.containsKey(candidateKey)) {
            candidateKey = "${baseKey}_$counter"
            counter++
        }
        nextSuffix[baseKey] = counter
        result[candidateKey] = value.toAnyValue(path)
    }
    return AnyValue.MapValue(result)
}
