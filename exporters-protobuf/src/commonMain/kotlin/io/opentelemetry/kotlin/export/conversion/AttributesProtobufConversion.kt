package io.opentelemetry.kotlin.export.conversion

import io.opentelemetry.kotlin.attributes.AttributeValue
import io.opentelemetry.kotlin.attributes.toAttributeValues
import io.opentelemetry.kotlin.attributes.AnyValue as KotlinAnyValue
import io.opentelemetry.kotlin.proto.common.v1.AnyValue
import io.opentelemetry.kotlin.proto.common.v1.ArrayValue
import io.opentelemetry.kotlin.proto.common.v1.KeyValue
import io.opentelemetry.kotlin.proto.common.v1.KeyValueList
import okio.ByteString

fun Map<String, Any>.createKeyValues(): List<KeyValue> =
    toAttributeValues().map { (key, value) -> KeyValue(key = key, value_ = value.toProtoAnyValue()) }

/**
 * Decodes protobuf attributes into the form exposed by `AttributeContainer.attributes`. Keys
 * without a value, or with an empty value, are dropped because an attribute can't be null.
 */
internal fun List<KeyValue>.toAttributeMap(): Map<String, Any> = mapNotNull { entry ->
    entry.value_?.toAttributeValue()?.let { entry.key to it.toAny() }
}.toMap()

internal fun AttributeValue.toProtoAnyValue(): AnyValue = when (this) {
    is AttributeValue.Str -> AnyValue(string_value = value)
    is AttributeValue.Bool -> AnyValue(bool_value = value)
    is AttributeValue.Long -> AnyValue(int_value = value)
    is AttributeValue.Double -> AnyValue(double_value = value)
    is AttributeValue.StrList -> value.toProtoArray { AnyValue(string_value = it) }
    is AttributeValue.BoolList -> value.toProtoArray { AnyValue(bool_value = it) }
    is AttributeValue.LongList -> value.toProtoArray { AnyValue(int_value = it) }
    is AttributeValue.DoubleList -> value.toProtoArray { AnyValue(double_value = it) }
    is AttributeValue.Value -> value.toProtoAnyValue()
}

private inline fun <T> List<T>.toProtoArray(convert: (T) -> AnyValue): AnyValue =
    AnyValue(array_value = ArrayValue(values = map(convert)))

/**
 * Decodes a protobuf value into an [AttributeValue]. Arrays whose elements all hold the same
 * primitive type become the matching typed list; any other array, along with bytes and kvlists,
 * becomes an [AttributeValue.Value] so that no element is dropped. Returns null for an empty value.
 */
internal fun AnyValue.toAttributeValue(): AttributeValue? = when {
    string_value != null -> AttributeValue.Str(string_value)
    bool_value != null -> AttributeValue.Bool(bool_value)
    int_value != null -> AttributeValue.Long(int_value)
    double_value != null -> AttributeValue.Double(double_value)
    array_value != null -> array_value.toAttributeValue()
    bytes_value != null || kvlist_value != null -> AttributeValue.Value(toNestedAnyValue())
    else -> null
}

private fun ArrayValue.toAttributeValue(): AttributeValue =
    values.allOf { it.string_value }?.let { AttributeValue.StrList(it) }
        ?: values.allOf { it.bool_value }?.let { AttributeValue.BoolList(it) }
        ?: values.allOf { it.int_value }?.let { AttributeValue.LongList(it) }
        ?: values.allOf { it.double_value }?.let { AttributeValue.DoubleList(it) }
        ?: AttributeValue.Value(KotlinAnyValue.ListValue(values.map { it.toNestedAnyValue() }))

/**
 * Returns [field] of every element, or null if any element doesn't hold that field.
 */
private inline fun <T : Any> List<AnyValue>.allOf(field: (AnyValue) -> T?): List<T>? =
    mapNotNull(field).takeIf { it.size == size }

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
