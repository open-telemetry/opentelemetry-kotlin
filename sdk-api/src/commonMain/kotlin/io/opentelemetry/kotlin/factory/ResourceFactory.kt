package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.resource.Entity
import io.opentelemetry.kotlin.resource.EntityAttributesMutator
import io.opentelemetry.kotlin.resource.Resource

/**
 * Produces [Resource] and [Entity] objects.
 *
 * See https://opentelemetry.io/docs/specs/otel/resource/
 */
@ExperimentalApi
public interface ResourceFactory {

    /**
     * An empty [Resource] with no attributes and no schema URL.
     */
    public val empty: Resource

    /**
     * Creates a [Resource] with the given schema URL and attributes.
     */
    public fun create(schemaUrl: String? = null, attributes: AttributesMutator.() -> Unit): Resource

    /**
     * Creates an [Entity] with the given schema URL and attributes.
     *
     * Returns `null` if [type] is empty or no identifying attributes are provided.
     *
     * https://opentelemetry.io/docs/specs/otel/entities/data-model/
     */
    public fun createEntity(
        type: String,
        schemaUrl: String? = null,
        attributes: EntityAttributesMutator.() -> Unit
    ): Entity? = null
}
