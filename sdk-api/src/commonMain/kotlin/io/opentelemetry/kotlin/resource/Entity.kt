package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * An entity as described in the OTel specification.
 *
 * https://opentelemetry.io/docs/specs/otel/entities/data-model/
 */
@ExperimentalApi
public interface Entity {

    /**
     * Defines the type of the entity. Must not be empty.
     */
    public val type: String

    /**
     * A schema URL for this entity, if available.
     */
    public val schemaUrl: String?

    /**
     * Attributes that identify the entity. Must contain at least one attribute.
     */
    public val id: Map<String, Any>

    /**
     * Descriptive (non-identifying) attributes of the entity.
     */
    public val description: Map<String, Any>
}
