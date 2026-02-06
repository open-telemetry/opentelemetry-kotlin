package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer

/**
 * Defines configuration for a resource.
 */
@ExperimentalApi
public interface ResourceConfigDsl {

    /**
     * Declares a resource, including an optional schema and any attributes.
     */
    public fun resource(schemaUrl: String? = null, attributes: MutableAttributeContainer.() -> Unit)

    /**
     * Declares a resource by taking a copy of the supplied Map values.
     */
    public fun resource(map: Map<String, Any>)
}
