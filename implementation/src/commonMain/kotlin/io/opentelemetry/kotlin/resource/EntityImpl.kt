package io.opentelemetry.kotlin.resource

internal class EntityImpl(
    override val type: String,
    override val schemaUrl: String?,
    override val id: Map<String, Any>,
    override val description: Map<String, Any>
) : Entity {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is EntityImpl) {
            return false
        }
        return type == other.type &&
            schemaUrl == other.schemaUrl &&
            id == other.id &&
            description == other.description
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (schemaUrl?.hashCode() ?: 0)
        result = 31 * result + id.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }
}
