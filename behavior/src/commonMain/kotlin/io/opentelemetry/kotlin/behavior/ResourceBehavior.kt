package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * The service producing telemetry, which on a mobile platform is the app itself.
 *
 * https://opentelemetry.io/docs/specs/otel/resource/sdk/
 */
@ExperimentalApi
data class ResourceBehavior(

    /**
     * Logical name of the service, which takes precedence over any `service.name` supplied in
     * [attributes] by the same layer.
     */
    val serviceName: String? = null,

    /**
     * URL of the schema that [attributes] conform to.
     */
    val schemaUrl: String? = null,

    /**
     * Attributes describing the service producing telemetry.
     */
    val attributes: Map<String, Any>? = null,
) : Behavior<ResourceBehavior> {

    override fun mergeWith(higher: ResourceBehavior): ResourceBehavior = copy(
        serviceName = higher.serviceName ?: serviceName,
        schemaUrl = higher.schemaUrl ?: schemaUrl,
        attributes = mergeMap(attributes, higher.attributes),
    )
}
