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

    private fun Map<String, Any>.limit(): MutableMap<String, Any> =
        entries.take(DEFAULT_ATTRIBUTE_LIMIT).associate { it.toPair() }.toMutableMap()
}
