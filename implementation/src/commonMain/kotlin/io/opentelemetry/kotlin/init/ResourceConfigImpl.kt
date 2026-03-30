package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.BuildKonfig
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes

internal fun sdkDefaultResource(): Resource = ResourceImpl(
    container = AttributesModel(
        attrs = mutableMapOf(
            ServiceAttributes.SERVICE_NAME to "unknown_service",
            ServiceAttributes.SERVICE_VERSION to BuildKonfig.SDK_VERSION,
            TelemetryAttributes.TELEMETRY_SDK_NAME to "opentelemetry",
            TelemetryAttributes.TELEMETRY_SDK_LANGUAGE to "kotlin",
            TelemetryAttributes.TELEMETRY_SDK_VERSION to BuildKonfig.SDK_VERSION,
        ),
    ),
    schemaUrl = null,
)

internal class ResourceConfigImpl : ResourceConfigDsl {

    private val resourceAttrs = AttributesModel(DEFAULT_ATTRIBUTE_LIMIT)
    private var schemaUrl: String? = null
    private var serviceNameOverride: String? = null

    override var serviceName: String
        get() = serviceNameOverride ?: "unknown_service"
        set(value) {
            serviceNameOverride = value
        }

    override fun resource(
        schemaUrl: String?,
        attributes: AttributesMutator.() -> Unit
    ) {
        this.schemaUrl = schemaUrl
        resourceAttrs.attributes()
    }

    override fun resource(map: Map<String, Any>) {
        resource {
            setAttributes(map)
        }
    }

    internal fun generateResource(): Resource {
        val attrs = resourceAttrs.attributes.toMutableMap()
        serviceNameOverride?.let { attrs[ServiceAttributes.SERVICE_NAME] = it }
        return ResourceImpl(
            schemaUrl = schemaUrl,
            container = AttributesModel(attributeLimit = DEFAULT_ATTRIBUTE_LIMIT, attrs = attrs)
        )
    }
}
