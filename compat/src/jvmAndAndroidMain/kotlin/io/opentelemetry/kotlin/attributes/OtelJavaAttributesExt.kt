package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.resource.Resource

internal fun OtelJavaAttributes.convertToMap(): Map<String, Any> {
    return this.asMap().entries.associate { (key, value) ->
        key.key to when (value) {
            is OtelJavaValue<*> -> value.toOtelKotlinAnyValue()
            else -> value
        }
    }
}

/**
 * Converts an attribute map to Java OTel's [OtelJavaAttributes], preserving each value's type.
 */
internal fun attrsFromMap(map: Map<String, Any>): OtelJavaAttributes =
    CompatAttributesModel().apply { setTypedAttributes(map) }.otelJavaAttributes()

/**
 * Converts any [AttributeContainer] to [OtelJavaAttributes], avoiding a copy when it is already compat-backed.
 */
internal fun AttributeContainer.toOtelJavaAttributes(): OtelJavaAttributes =
    (this as? CompatAttributesModel)?.otelJavaAttributes() ?: attrsFromMap(attributes)

/**
 * Converts a single attribute value to the type [key] expects, without converting any other attributes.
 */
internal fun <T> Map<String, Any>.getOtelJavaAttribute(key: OtelJavaAttributeKey<T>): T? {
    val value = this[key.key] ?: return null
    return attrsFromMap(mapOf(key.key to value)).get(key)
}

private class ResourceConversion(val resource: Resource, val converted: OtelJavaResource)

/**
 * Most recent [resourceFromMap] result. A process almost always has a single [Resource], so this
 * avoids rebuilding the same Java resource for every exported span or log.
 */
@Volatile
private var lastResourceConversion: ResourceConversion? = null

/**
 * Converts a [Resource] to [OtelJavaResource]. Resources are immutable, so the result for the most
 * recently converted instance is reused.
 */
internal fun resourceFromMap(resource: Resource): OtelJavaResource {
    lastResourceConversion?.takeIf { it.resource === resource }?.let { return it.converted }
    val converted = OtelJavaResource.create(attrsFromMap(resource.attributes), resource.schemaUrl)
    lastResourceConversion = ResourceConversion(resource, converted)
    return converted
}
