package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.config.schema.model.AttributeNameValue
import io.opentelemetry.kotlin.config.schema.model.AttributeType
import io.opentelemetry.kotlin.config.schema.model.Resource
import io.opentelemetry.kotlin.propagation.utils.parseResourceAttributes

/**
 * Maps declarative resource configuration onto the behavior it supplies.
 */
@ExperimentalApi
fun Resource.toBehavior(): ResourceBehavior {
    return ResourceBehavior(
        schemaUrl = schemaUrl,
        attributes = toAttributeMap().takeUnless { it.isEmpty() },
    )
}

private fun Resource.toAttributeMap(): Map<String, Any> = buildMap {
    putAll(attributesList?.let(::parseResourceAttributes).orEmpty())
    attributes.orEmpty()
        .mapNotNull { it.toAttribute() }
        .forEach { (name, value) -> put(name, value) }
}

private fun AttributeNameValue.toAttribute(): Pair<String, Any>? {
    if (name.isEmpty()) {
        return null
    }
    val attributeValue = value ?: return null
    val mappedValue = when (type ?: AttributeType.STRING) {
        AttributeType.STRING -> attributeValue as? String
        AttributeType.BOOL -> attributeValue as? Boolean
        AttributeType.INT -> (attributeValue as? Number)?.toLongIfWhole()
        AttributeType.DOUBLE -> (attributeValue as? Number)?.toDouble()
        AttributeType.STRING_ARRAY -> attributeValue.toListOf<String>()
        AttributeType.BOOL_ARRAY -> attributeValue.toListOf<Boolean>()
        AttributeType.INT_ARRAY -> attributeValue.toListOf<Number>()?.toLongListIfWhole()
        AttributeType.DOUBLE_ARRAY -> attributeValue.toListOf<Number>()?.map { it.toDouble() }
    } ?: return null

    return name to mappedValue
}

private fun Number.toLongIfWhole(): Long? {
    val doubleValue = toDouble()
    return toLong().takeIf { doubleValue.isFinite() && doubleValue == it.toDouble() }
}

private inline fun <reified T> Any.toListOf(): List<T>? {
    val list = this as? List<*> ?: return null
    return list.map { it as? T ?: return null }
}

private fun List<Number>.toLongListIfWhole(): List<Long>? {
    return map { it.toLongIfWhole() ?: return null }
}
