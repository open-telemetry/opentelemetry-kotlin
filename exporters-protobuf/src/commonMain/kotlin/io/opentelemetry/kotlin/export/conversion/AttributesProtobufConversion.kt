package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.attributes.AnyValue as KotlinAnyValue
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.common.v1.ArrayValue
import io.opentelemetry.proto.common.v1.KeyValue
import io.opentelemetry.proto.common.v1.KeyValueList
import okio.ByteString

fun Map<String, Any>.createKeyValues(): List<KeyValue> = map(::createKeyValue)

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
    bytes_value != null -> bytes_value.toByteArray()
    array_value != null -> array_value.values.mapNotNull(AnyValue::toAttributeValue)
    kvlist_value != null -> kvlist_value.toMapValue()
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
    is Float -> AnyValue(double_value = value.toDouble())
    is Number -> AnyValue(int_value = value.toLong())
    is Boolean -> AnyValue(bool_value = value)
    is ByteArray -> AnyValue(bytes_value = ByteString.of(*value))
    is List<*> -> AnyValue(array_value = handleList(value as List<Any>))
    is KotlinAnyValue -> value.toProtoAnyValue()
    else -> throw UnsupportedOperationException()
}

private fun handleList(elements: List<Any>) = ArrayValue(
    elements.map(::convertAttributeValue)
)

internal fun KotlinAnyValue.toProtoAnyValue(): AnyValue = when (this) {
    is KotlinAnyValue.NullValue -> AnyValue()
    is KotlinAnyValue.StringValue -> AnyValue(string_value = value)
    is KotlinAnyValue.BoolValue -> AnyValue(bool_value = value)
    is KotlinAnyValue.LongValue -> AnyValue(int_value = value)
    is KotlinAnyValue.DoubleValue -> AnyValue(double_value = value)
    is KotlinAnyValue.BytesValue -> AnyValue(bytes_value = ByteString.of(*value))
    is KotlinAnyValue.ListValue -> AnyValue(
        array_value = ArrayValue(values = values.map { it.toProtoAnyValue() })
    )
    is KotlinAnyValue.MapValue -> AnyValue(
        kvlist_value = KeyValueList(
            values = values.map { (key, value) -> KeyValue(key = key, value_ = value.toProtoAnyValue()) }
        )
    )
}

internal fun AnyValue.toNestedAnyValue(): KotlinAnyValue = when {
    string_value != null -> KotlinAnyValue.StringValue(string_value)
    bool_value != null -> KotlinAnyValue.BoolValue(bool_value)
    int_value != null -> KotlinAnyValue.LongValue(int_value)
    double_value != null -> KotlinAnyValue.DoubleValue(double_value)
    bytes_value != null -> KotlinAnyValue.BytesValue(bytes_value.toByteArray())
    array_value != null -> KotlinAnyValue.ListValue(
        values = array_value.values.map { it.toNestedAnyValue() }
    )
    kvlist_value != null -> kvlist_value.toMapValue()
    else -> KotlinAnyValue.NullValue
}

internal fun KeyValueList.toMapValue(): KotlinAnyValue.MapValue = KotlinAnyValue.MapValue(
    values = values.associate { it.key to (it.value_?.toNestedAnyValue() ?: KotlinAnyValue.NullValue) }
)
