package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeType
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaValue

/**
 * Converts an opentelemetry-java attribute value to an [AttributeValue] using the type declared by
 * this key rather than inferring one from [value]'s runtime type. Returns null if [value] doesn't
 * match that type, including a list with any element of the wrong type, or if the type is unknown.
 */
internal fun OtelJavaAttributeKey<*>.toAttributeValue(value: Any): AttributeValue? = when (type) {
    OtelJavaAttributeType.STRING -> (value as? String)?.let { AttributeValue.Str(it) }
    OtelJavaAttributeType.BOOLEAN -> (value as? Boolean)?.let { AttributeValue.Bool(it) }
    OtelJavaAttributeType.LONG -> (value as? Long)?.let { AttributeValue.Long(it) }
    OtelJavaAttributeType.DOUBLE -> (value as? Double)?.let { AttributeValue.Double(it) }
    OtelJavaAttributeType.STRING_ARRAY -> value.asListOf<String>()?.let { AttributeValue.StrList(it) }
    OtelJavaAttributeType.BOOLEAN_ARRAY -> value.asListOf<Boolean>()?.let { AttributeValue.BoolList(it) }
    OtelJavaAttributeType.LONG_ARRAY -> value.asListOf<Long>()?.let { AttributeValue.LongList(it) }
    OtelJavaAttributeType.DOUBLE_ARRAY -> value.asListOf<Double>()?.let { AttributeValue.DoubleList(it) }
    OtelJavaAttributeType.VALUE -> (value as? OtelJavaValue<*>)?.let { AttributeValue.Value(it.toOtelKotlinAnyValue()) }
    else -> null
}

/**
 * Sets a single opentelemetry-java attribute, silently ignoring it if [value] doesn't match the key's type.
 */
internal fun AttributesMutator.setOtelJavaAttribute(key: OtelJavaAttributeKey<*>, value: Any) {
    key.toAttributeValue(value)?.applyTo(key.key, this)
}

/**
 * Sets every opentelemetry-java attribute, silently ignoring any whose value doesn't match its key's type.
 */
internal fun AttributesMutator.setOtelJavaAttributes(attributes: OtelJavaAttributes) {
    attributes.forEach { key, value -> setOtelJavaAttribute(key, value) }
}

/**
 * Sets every attribute in a user-supplied map, inferring each value's type with
 * [AttributeValue.fromPublic].
 */
internal fun AttributesMutator.setAttributeMap(attributes: Map<String, Any>) {
    attributes.forEach { (key, value) -> AttributeValue.fromPublic(value).applyTo(key, this) }
}

/**
 * Converts to the value opentelemetry-java stores under [key], or null if this case doesn't match
 * the key's type or the type is unknown.
 */
internal fun <T> AttributeValue.toOtelJavaAttributeValue(key: OtelJavaAttributeKey<T>): T? {
    val value: Any? = when (key.type) {
        OtelJavaAttributeType.STRING -> (this as? AttributeValue.Str)?.value
        OtelJavaAttributeType.BOOLEAN -> (this as? AttributeValue.Bool)?.value
        OtelJavaAttributeType.LONG -> (this as? AttributeValue.Long)?.value
        OtelJavaAttributeType.DOUBLE -> (this as? AttributeValue.Double)?.value
        OtelJavaAttributeType.STRING_ARRAY -> (this as? AttributeValue.StrList)?.value
        OtelJavaAttributeType.BOOLEAN_ARRAY -> (this as? AttributeValue.BoolList)?.value
        OtelJavaAttributeType.LONG_ARRAY -> (this as? AttributeValue.LongList)?.value
        OtelJavaAttributeType.DOUBLE_ARRAY -> (this as? AttributeValue.DoubleList)?.value
        OtelJavaAttributeType.VALUE -> (this as? AttributeValue.Value)?.value?.toOtelJavaValue()
        else -> null
    }
    @Suppress("UNCHECKED_CAST")
    return value as T?
}

@Suppress("UNCHECKED_CAST")
private inline fun <reified T> Any.asListOf(): List<T>? =
    (this as? List<*>)?.takeIf { list -> list.all { it is T } } as List<T>?
