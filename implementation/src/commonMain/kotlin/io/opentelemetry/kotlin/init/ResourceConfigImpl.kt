package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl

internal class ResourceConfigImpl : ResourceConfigDsl {

    private val resourceAttrs = AttributesModel(DEFAULT_ATTRIBUTE_LIMIT)
    private var schemaUrl: String? = null

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

    fun generateResource(): Resource = ResourceImpl(
        schemaUrl = schemaUrl,
        container = AttributesModel(
            DEFAULT_ATTRIBUTE_LIMIT,
            resourceAttrs.attributes.toMutableMap()
        )
    )
}
