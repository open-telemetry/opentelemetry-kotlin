package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl
import io.opentelemetry.kotlin.semconv.ServiceAttributes

internal class ResourceConfigImpl : ResourceConfigDsl {

    private val resourceAttrs = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT)
    private var schemaUrl: String? = null

    override var serviceName: String? = null

    override fun resource(
        schemaUrl: String?,
        attributes: AttributesMutator.() -> Unit
    ) {
        this.schemaUrl = schemaUrl
        resourceAttrs.attributes()
    }

    override fun resource(map: Map<String, Any>) {
        resource {
            setAttributes(map)
        }
    }

    internal fun generateResource(): Resource {
        val attrs = resourceAttrs.attributes.toMutableMap()
        serviceName?.let { attrs[ServiceAttributes.SERVICE_NAME] = it }
        return ResourceImpl(
            schemaUrl = schemaUrl,
            container = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT, attrs = attrs)
        )
    }
}
