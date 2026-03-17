package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.resource.Resource

/**
 * Produces [Resource] objects.
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
}
