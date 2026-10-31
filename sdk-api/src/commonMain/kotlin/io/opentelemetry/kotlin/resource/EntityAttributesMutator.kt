package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * Configures the identifying and descriptive attributes of an [Entity].
 *
 * https://opentelemetry.io/docs/specs/otel/entities/data-model/
 */
@ExperimentalApi
public interface EntityAttributesMutator {

    /**
     * Attributes that identify the entity. Must contain at least one attribute.
     */
    public fun id(action: AttributesMutator.() -> Unit)

    /**
     * Descriptive (non-identifying) attributes of the entity.
     */
    public fun description(action: AttributesMutator.() -> Unit)
}
