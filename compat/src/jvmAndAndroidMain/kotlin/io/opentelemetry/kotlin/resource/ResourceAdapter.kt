package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaResourceBuilder
import io.opentelemetry.kotlin.attributes.convertToMap

internal class ResourceAdapter(
    impl: OtelJavaResource
) : Resource {
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
    override val schemaUrl: String? = impl.schemaUrl

    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        val impl = MutableResourceImpl(attributes.toMutableMap(), schemaUrl)
        impl.apply(action)

        val builder = OtelJavaResourceBuilder()
        impl.schemaUrl?.let(builder::setSchemaUrl)

        impl.attributes.forEach {
            builder.putTyped(it.key, it.value)
        }
        return ResourceAdapter(builder.build())
    }

    override fun merge(other: Resource): Resource {
        val mergedAttrs = attributes + other.attributes
        val mergedSchema = when {
            schemaUrl == null -> other.schemaUrl
            other.schemaUrl == null -> schemaUrl
            schemaUrl == other.schemaUrl -> schemaUrl
            else -> other.schemaUrl
        }
        val builder = OtelJavaResourceBuilder()
        mergedSchema?.let(builder::setSchemaUrl)
        mergedAttrs.forEach { builder.putTyped(it.key, it.value) }
        return ResourceAdapter(builder.build())
    }
}

@Suppress("UNCHECKED_CAST", "SpreadOperator")
private fun OtelJavaResourceBuilder.putTyped(key: String, value: Any) {
    when (value) {
        is String -> put(key, value)
        is Long -> put(key, value)
        is Double -> put(key, value)
        is Boolean -> put(key, value)
        is List<*> -> when {
            value.all { it is String } -> put(key, *value.map { it as String }.toTypedArray())
            value.all { it is Long } -> put(key, *value.map { it as Long }.toLongArray())
            value.all { it is Double } -> put(key, *value.map { it as Double }.toDoubleArray())
            value.all { it is Boolean } -> put(key, *value.map { it as Boolean }.toBooleanArray())
            else -> put(key, value.toString())
        }
        else -> put(key, value.toString())
    }
}
