package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl

internal class ResourceFactoryImpl : ResourceFactory {

    override val empty: Resource = ResourceImpl(AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT), null)

    override fun create(schemaUrl: String?, attributes: AttributesMutator.() -> Unit): Resource {
        val attrs = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT).apply { attributes() }
        return ResourceImpl(attrs, schemaUrl)
    }
}
