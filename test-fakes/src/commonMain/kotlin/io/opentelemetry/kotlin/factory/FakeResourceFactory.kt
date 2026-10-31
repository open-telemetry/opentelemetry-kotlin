package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.resource.Entity
import io.opentelemetry.kotlin.resource.EntityAttributesMutator
import io.opentelemetry.kotlin.resource.FakeEntity
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.Resource

class FakeResourceFactory : ResourceFactory {

    override val empty: Resource = FakeResource(attributes = emptyMap(), schemaUrl = null)

    override fun create(schemaUrl: String?, attributes: AttributesMutator.() -> Unit): Resource =
        FakeResource(attributes = emptyMap(), schemaUrl = schemaUrl)

    override fun createEntity(
        type: String,
        schemaUrl: String?,
        attributes: EntityAttributesMutator.() -> Unit
    ): Entity? = FakeEntity(type = type, schemaUrl = schemaUrl)
}
