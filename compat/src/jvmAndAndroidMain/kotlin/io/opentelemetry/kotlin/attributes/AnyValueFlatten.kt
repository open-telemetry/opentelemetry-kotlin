package io.opentelemetry.kotlin.attributes

/**
 * Java OTel's Attributes API has no AnyValue analogue. Flatten the primitive
 * AnyValue variants to their equivalent attribute types; drop the rest
 * (null, bytes, list, map) since they cannot be represented.
 */
internal fun AttributesMutator.setFlattenedAnyValueAttribute(key: String, value: AnyValue) {
    when (value) {
        is AnyValue.StringValue -> setStringAttribute(key, value.value)
        is AnyValue.BoolValue -> setBooleanAttribute(key, value.value)
        is AnyValue.LongValue -> setLongAttribute(key, value.value)
        is AnyValue.DoubleValue -> setDoubleAttribute(key, value.value)
        AnyValue.NullValue,
        is AnyValue.BytesValue,
        is AnyValue.ListValue,
        is AnyValue.MapValue -> {}
    }
}
