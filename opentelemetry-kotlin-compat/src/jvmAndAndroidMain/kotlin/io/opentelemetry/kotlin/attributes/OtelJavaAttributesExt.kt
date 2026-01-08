package io.opentelemetry.kotlin.attributes

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.resource.Resource

internal fun OtelJavaAttributes.convertToMap(): Map<String, Any> {
    return this.asMap().mapKeys { it.key.key }
}

internal fun attrsFromMap(map: Map<String, Any>): OtelJavaAttributes {
    val builder = OtelJavaAttributes.builder()
    map.forEach {
        builder.put(it.key, it.value.toString())
    }
    return builder.build()
}

@OptIn(ExperimentalApi::class)
internal fun resourceFromMap(resource: Resource): OtelJavaResource {
    val map = resource.attributes
    val schemaUrl = resource.schemaUrl
    return OtelJavaResource.create(
        attrsFromMap(map),
        schemaUrl
    )
}
