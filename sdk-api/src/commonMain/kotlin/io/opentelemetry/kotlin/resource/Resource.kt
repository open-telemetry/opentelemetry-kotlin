package io.opentelemetry.kotlin.resource

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributeContainer

/**
 * Implementations of this interface hold a 'resource' as described in the OTel specification.
 *
 * https://opentelemetry.io/docs/specs/otel/resource/data-model/
 */
@ExperimentalApi
@ThreadSafe
public interface Resource : AttributeContainer {

    /**
     * A schema URL for this resource, if available.
     */
    public val schemaUrl: String?

    /**
     * Creates a new resource using the current instance as a template.
     */
    public fun asNewResource(action: MutableResource.() -> Unit): Resource
}
