package io.opentelemetry.kotlin.attributes

import io.opentelemetry.api.common.AttributeType
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaValue

/**
 * Copies an attribute map onto an [AttributesMutator], preserving the double-ness of its values.
 *
 * The common [setAttributes] widens any whole-valued number to a long, because Kotlin/JS cannot
 * tell an [Int] from a [Double] at runtime. Java OTel attributes are statically typed, so that
 * ambiguity does not exist here and a double must survive the round trip as a double. Doubles are
 * therefore dispatched here and everything else is delegated to the common implementation.
 */
internal fun AttributesMutator.setTypedAttributes(attributes: Map<String, Any>) {
    attributes.forEach { setTypedAttribute(it.key, it.value) }
}

internal fun AttributesMutator.setTypedAttributes(attributes: OtelJavaAttributes) {
    attributes.forEach { key, value -> setTypedAttribute(key, value) }
}

internal fun AttributesMutator.setTypedAttribute(key: OtelJavaAttributeKey<*>, value: Any) {
    val name = key.key
    when (key.type) {
        AttributeType.BOOLEAN -> if (value is Boolean) {
            setBooleanAttribute(name, value)
            return
        }
        AttributeType.STRING -> if (value is String) {
            setStringAttribute(name, value)
            return
        }
        AttributeType.LONG -> if (value is Long) {
            setLongAttribute(name, value)
            return
        }
        AttributeType.DOUBLE -> if (value is Double) {
            setDoubleAttribute(name, value)
            return
        }
        AttributeType.BOOLEAN_ARRAY -> value.asTypedList<Boolean>()?.let {
            setBooleanListAttribute(name, it)
            return
        }
        AttributeType.STRING_ARRAY -> value.asTypedList<String>()?.let {
            setStringListAttribute(name, it)
            return
        }
        AttributeType.LONG_ARRAY -> value.asTypedList<Long>()?.let {
            setLongListAttribute(name, it)
            return
        }
        AttributeType.DOUBLE_ARRAY -> value.asTypedList<Double>()?.let {
            setDoubleListAttribute(name, it)
            return
        }
        AttributeType.VALUE -> if (value is OtelJavaValue<*>) {
            setAnyValueAttribute(name, value.toOtelKotlinAnyValue())
            return
        }
    }
    setTypedAttribute(name, value)
}

internal fun AttributesMutator.setTypedAttribute(key: String, value: Any) {
    when {
        value is OtelJavaValue<*> -> setAnyValueAttribute(key, value.toOtelKotlinAnyValue())
        value is Double -> setDoubleAttribute(key, value)
        value is List<*> && value.isNotEmpty() && value.all { it is Double } ->
            setDoubleListAttribute(key, value.filterIsInstance<Double>())
        else -> setAttributes(mapOf(key to value))
    }
}

private inline fun <reified T> Any.asTypedList(): List<T>? {
    val values = this as? List<*> ?: return null
    if (!values.all { it is T }) {
        return null
    }
    @Suppress("UNCHECKED_CAST")
    return values as List<T>
}
