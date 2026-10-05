package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.convertToMap

internal class ResourceAdapter(
    impl: OtelJavaResource
) : Resource {
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
    override val schemaUrl: String? = impl.schemaUrl

    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        val impl = MutableResourceImpl(attributes.toMutableMap(), schemaUrl)
        impl.apply(action)
        return ResourceAdapter(
            OtelJavaResource.create(attrsFromMap(impl.attributes), impl.schemaUrl)
        )
    }

    override fun merge(other: Resource): Resource {
        val mergedAttrs = attributes + other.attributes
        val mergedSchema = when {
            schemaUrl == null -> other.schemaUrl
            other.schemaUrl == null -> schemaUrl
            schemaUrl == other.schemaUrl -> schemaUrl
            else -> other.schemaUrl
        }
        return ResourceAdapter(OtelJavaResource.create(attrsFromMap(mergedAttrs), mergedSchema))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ResourceAdapter) {
            return false
        }
        return attributes == other.attributes && schemaUrl == other.schemaUrl
    }

    override fun hashCode(): Int {
        var result = attributes.hashCode()
        result = 31 * result + (schemaUrl?.hashCode() ?: 0)
        return result
    }
}
