package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.resource.Resource

internal fun OtelJavaAttributes.convertToMap(): Map<String, Any> {
    return this.asMap().mapKeys { it.key.key }
}

/**
 * Converts an attribute map to Java OTel's [OtelJavaAttributes], preserving each value's type.
 */
internal fun attrsFromMap(map: Map<String, Any>): OtelJavaAttributes =
    CompatAttributesModel().apply { setTypedAttributes(map) }.otelJavaAttributes()

internal fun resourceFromMap(resource: Resource): OtelJavaResource {
    val map = resource.attributes
    val schemaUrl = resource.schemaUrl
    return OtelJavaResource.create(
        attrsFromMap(map),
        schemaUrl
    )
}
