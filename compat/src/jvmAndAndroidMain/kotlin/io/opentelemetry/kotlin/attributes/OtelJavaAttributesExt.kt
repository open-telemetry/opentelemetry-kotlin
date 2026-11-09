package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.resource.Resource

/**
 * Converts to the map form exposed by [AttributeContainer.attributes], dropping any value that
 * doesn't match its key's type.
 */
internal fun OtelJavaAttributes.convertToMap(): Map<String, Any> {
    val result = LinkedHashMap<String, Any>(size())
    forEach { key, value -> key.toAttributeValue(value)?.let { result[key.key] = it.toAny() } }
    return result
}

/**
 * Converts an attribute map to Java OTel's [OtelJavaAttributes], inferring each value's type with
 * [AttributeValue.fromPublic].
 */
internal fun attrsFromMap(map: Map<String, Any>): OtelJavaAttributes =
    buildOtelJavaAttributes { setAttributeMap(map) }

/**
 * Converts any [AttributeContainer] to [OtelJavaAttributes], avoiding a copy when it is already compat-backed.
 */
internal fun AttributeContainer.toOtelJavaAttributes(): OtelJavaAttributes {
    if (this is CompatAttributesModel) {
        return otelJavaAttributes()
    }
    val typed = typedAttributesOrConvert()
    return buildOtelJavaAttributes { typed.forEach { (key, value) -> value.applyTo(key, this) } }
}

/**
 * Reads [key] from this map without converting any other attributes. The stored value's type is
 * inferred with [AttributeValue.fromPublic], so this returns null if the key is absent or the
 * inferred type differs from [key]'s type. For example, an empty list only matches a string-array key.
 *
 * opentelemetry-java stores a primitive [AnyValue] under its primitive type, so an [AnyValue] that
 * doesn't match [key] directly is read back the same way [attrsFromMap] would store it.
 */
internal fun <T> Map<String, Any>.getOtelJavaAttribute(key: OtelJavaAttributeKey<T>): T? {
    val value = this[key.key] ?: return null
    val attributeValue = AttributeValue.fromPublic(value)
    attributeValue.toOtelJavaAttributeValue(key)?.let { return it }
    if (attributeValue !is AttributeValue.Value) {
        return null
    }
    return buildOtelJavaAttributes { attributeValue.applyTo(key.key, this) }.get(key)
}

private inline fun buildOtelJavaAttributes(action: AttributesMutator.() -> Unit): OtelJavaAttributes {
    val builder = OtelJavaAttributes.builder()
    OtelJavaAttributesMutator(builder.attributeSink()).action()
    return builder.build()
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
