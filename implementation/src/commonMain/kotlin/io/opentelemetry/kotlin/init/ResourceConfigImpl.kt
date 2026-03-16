package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.BuildKonfig
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceImpl
import io.opentelemetry.kotlin.semconv.TelemetryAttributes

internal class ResourceConfigImpl : ResourceConfigDsl {

    private val resourceAttrs = AttributesModel(DEFAULT_ATTRIBUTE_LIMIT)
    private var schemaUrl: String? = null

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

    fun generateResource(): Resource {
        val sdkDefaults = mapOf(
            TelemetryAttributes.TELEMETRY_SDK_NAME to "opentelemetry",
            TelemetryAttributes.TELEMETRY_SDK_LANGUAGE to "kotlin",
            TelemetryAttributes.TELEMETRY_SDK_VERSION to BuildKonfig.SDK_VERSION,
        )
        val merged = (sdkDefaults + resourceAttrs.attributes).toMutableMap()
        return ResourceImpl(
            schemaUrl = schemaUrl,
            container = AttributesModel(DEFAULT_ATTRIBUTE_LIMIT, merged)
        )
    }
}
