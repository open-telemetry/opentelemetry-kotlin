package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.ArrayValue
import io.opentelemetry.proto.common.v1.KeyValue

@OptIn(ExperimentalApi::class)
fun Map<String, Any>.createKeyValues(): List<KeyValue> = map(::createKeyValue)

@OptIn(ExperimentalApi::class)
internal fun List<KeyValue>.toAttributeMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>()
    forEach { entry ->
        entry.value_?.toAttributeValue()?.let {
            map[entry.key] = it
        }
    }
    return map
}

private fun AnyValue.toAttributeValue(): Any? = when {
    string_value != null -> string_value
    int_value != null -> int_value
    double_value != null -> double_value
    bool_value != null -> bool_value
    array_value != null -> array_value.values.mapNotNull(AnyValue::toAttributeValue)
    else -> null
}

private fun createKeyValue(entry: Map.Entry<String, Any>) = KeyValue(
    key = entry.key, value_ = convertAttributeValue(entry.value)
)

@Suppress("UNCHECKED_CAST")
private fun convertAttributeValue(value: Any): AnyValue = when (value) {
    is String -> AnyValue(string_value = value)
    is Long -> AnyValue(int_value = value)
    is Double -> AnyValue(double_value = value)
    is Boolean -> AnyValue(bool_value = value)
    is List<*> -> AnyValue(array_value = handleList(value as List<Any>))
    else -> throw UnsupportedOperationException()
}

private fun handleList(elements: List<Any>) = ArrayValue(
    elements.map(::convertAttributeValue)
)
