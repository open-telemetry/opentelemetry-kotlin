package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.NO_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl
import io.opentelemetry.kotlin.semconv.ServiceAttributes

/**
 * Builds the [Resource] that this behavior describes, where [ResourceBehavior.serviceName] takes
 * precedence over any `service.name` in [ResourceBehavior.attributes].
 */
internal fun ResourceBehavior.toResource(): Resource {
    val attrs = attributes.orEmpty().toMutableMap()
    serviceName?.let { attrs[ServiceAttributes.SERVICE_NAME] = it }
    return ResourceImpl(
        schemaUrl = schemaUrl,
        container = AttributesModel(attributeLimit = NO_ATTRIBUTE_LIMIT, attrs = attrs)
    )
}
