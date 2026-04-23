package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT

internal class ResourceImpl(
    container: AttributeContainer,
    override val schemaUrl: String?,
) : Resource {

    override val attributes: Map<String, Any> = container.attributes

    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        val impl = MutableResourceImpl(attributes.toMutableMap(), schemaUrl)
        impl.apply(action)
        val container = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT, attrs = impl.attributes)
        return ResourceImpl(container, impl.schemaUrl)
    }

    override fun merge(other: Resource): Resource {
        val mergedAttrs = (attributes + other.attributes).toMutableMap()
        val mergedSchema = when {
            schemaUrl == null -> other.schemaUrl
            other.schemaUrl == null -> schemaUrl
            schemaUrl == other.schemaUrl -> schemaUrl
            else -> other.schemaUrl
        }
        return ResourceImpl(AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT, attrs = mergedAttrs), mergedSchema)
    }
}
