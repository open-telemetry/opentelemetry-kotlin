package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainerImpl

@OptIn(ExperimentalApi::class)
internal class ResourceImpl(
    container: MutableAttributeContainer,
    override val schemaUrl: String?,
) : Resource {

    override val attributes: Map<String, Any> = container.attributes.limit()

    override fun asNewResource(action: MutableResource.() -> Unit): Resource {
        val impl = MutableResourceImpl(attributes.limit(), schemaUrl)
        impl.apply(action)
        val container = MutableAttributeContainerImpl(attrs = impl.attributes.limit())
        return ResourceImpl(container, impl.schemaUrl)
    }

    private fun Map<String, Any>.limit(): MutableMap<String, Any> =
        entries.take(DEFAULT_ATTRIBUTE_LIMIT).associate { it.toPair() }.toMutableMap()
}
