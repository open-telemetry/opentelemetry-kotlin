package io.opentelemetry.kotlin.attributes

/**
 * Copies an attribute map onto an [AttributesMutator], dispatching each value to the setter that
 * matches its runtime type so that no type information is lost.
 */
internal fun AttributesMutator.setTypedAttributes(attributes: Map<String, Any>) {
    attributes.forEach { setTypedAttribute(it.key, it.value) }
}

internal fun AttributesMutator.setTypedAttribute(key: String, value: Any) {
    when (value) {
        is String -> setStringAttribute(key, value)
        is Boolean -> setBooleanAttribute(key, value)
        is Long -> setLongAttribute(key, value)
        is Double -> setDoubleAttribute(key, value)
        is Float -> setDoubleAttribute(key, value.toDouble())
        is Int, is Short, is Byte -> setLongAttribute(key, (value as Number).toLong())
        is ByteArray -> setByteArrayAttribute(key, value)
        is AnyValue -> setAnyValueAttribute(key, value)
        is Collection<*> -> setTypedListAttribute(key, value.toList())
        is Array<*> -> setTypedListAttribute(key, value.toList())
        else -> setStringAttribute(key, value.toString())
    }
}

@Suppress("UNCHECKED_CAST")
private fun AttributesMutator.setTypedListAttribute(key: String, value: List<*>) {
    // An empty list is reported as a list of strings, matching the common implementation.
    when {
        value.all { it is String } -> setStringListAttribute(key, value as List<String>)
        value.all { it is Boolean } -> setBooleanListAttribute(key, value as List<Boolean>)
        value.all { it is Long } -> setLongListAttribute(key, value as List<Long>)
        value.all { it is Double } -> setDoubleListAttribute(key, value as List<Double>)
        value.all { it is Float } -> setDoubleListAttribute(
            key,
            value.filterIsInstance<Float>().map(Float::toDouble)
        )
        value.all { it is Int || it is Short || it is Byte } -> setLongListAttribute(
            key,
            value.filterIsInstance<Number>().map(Number::toLong)
        )
        // A heterogeneous list has no typed equivalent, so fall back to a list of strings.
        else -> setStringListAttribute(key, value.map { it.toString() })
    }
}
