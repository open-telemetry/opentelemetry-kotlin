package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT

internal class EntityAttributesMutatorImpl : EntityAttributesMutator {
    private val idModel = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT)
    private val descriptionModel = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT)

    val idAttributes: Map<String, Any> get() = idModel.attributes
    val descriptionAttributes: Map<String, Any> get() = descriptionModel.attributes

    override fun id(action: AttributesMutator.() -> Unit) {
        idModel.apply(action)
    }

    override fun description(action: AttributesMutator.() -> Unit) {
        descriptionModel.apply(action)
    }
}
