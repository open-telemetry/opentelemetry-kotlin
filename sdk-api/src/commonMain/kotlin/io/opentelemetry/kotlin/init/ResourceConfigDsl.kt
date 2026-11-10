package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * Defines configuration for a resource.
 */
@ExperimentalApi
@ConfigDsl
public interface ResourceConfigDsl {

    /**
     * Sets the logical name of the service.
     * https://opentelemetry.io/docs/specs/semconv/registry/attributes/service/#service-attributes
     */
    public var serviceName: String?

    /**
     * Declares a resource, including an optional schema and any attributes.
     *
     * The [AttributesMutator] receiver does not carry the [ConfigDsl] marker, so the
     * enclosing configuration remains accessible implicitly inside [attributes]. Keep configuration
     * calls outside this lambda to avoid modifying the enclosing configuration unintentionally.
     */
    public fun resource(schemaUrl: String? = null, attributes: AttributesMutator.() -> Unit)

    /**
     * Declares a resource by taking a copy of the supplied Map values.
     */
    public fun resource(map: Map<String, Any>)
}
