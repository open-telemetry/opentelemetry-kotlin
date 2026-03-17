package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT

internal class ResourceImpl(
    container: AttributeContainer,
    override val schemaUrl: String?,
) : Resource {

    override val attributes: Map<String, Any> = container.attributes.limit()

    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        val impl = MutableResourceImpl(attributes.limit(), schemaUrl)
        impl.apply(action)
        val container = AttributesModel(attrs = impl.attributes.limit())
        return ResourceImpl(container, impl.schemaUrl)
    }

    override fun merge(other: Resource): Resource {
        val mergedAttrs = (attributes + other.attributes).limit()
        val mergedSchema = when {
            schemaUrl == null -> other.schemaUrl
            other.schemaUrl == null -> schemaUrl
            schemaUrl == other.schemaUrl -> schemaUrl
            else -> other.schemaUrl
        }
        return ResourceImpl(AttributesModel(attrs = mergedAttrs), mergedSchema)
    }

    internal fun Map<String, Any>.limit(): MutableMap<String, Any> =
        entries.take(DEFAULT_ATTRIBUTE_LIMIT).associate { it.toPair() }.toMutableMap()
}
