package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.attributes.toAttributeValues

internal class EntityImpl(
    override val type: String,
    override val schemaUrl: String?,
    override val id: Map<String, Any>,
    override val description: Map<String, Any>
) : Entity {

    private val typedId = id.toAttributeValues()

    /**
     * An entity is identified by its [type] and [id] only. [description] and [schemaUrl] are not
     * part of its identity.
     *
     * https://opentelemetry.io/docs/specs/otel/entities/data-model/
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is EntityImpl) {
            return false
        }
        return type == other.type && typedId == other.typedId
    }

    override fun hashCode(): Int = 31 * type.hashCode() + typedId.hashCode()
}
