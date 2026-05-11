package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProviderBuilder
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.metrics.MeterProviderAdapter
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.semconv.ServiceAttributes

@ExperimentalApi
internal class CompatMeterProviderConfig(
    private val clock: Clock,
) : MeterProviderConfigDsl {

    private val builder: OtelJavaSdkMeterProviderBuilder = OtelJavaSdkMeterProvider.builder()
    private var serviceNameOverride: String? = null

    override var serviceName: String
        get() = serviceNameOverride ?: "unknown_service"
        set(value) {
            serviceNameOverride = value
            resourceAttrs.setStringAttribute(ServiceAttributes.SERVICE_NAME, value)
        }

    private val resourceAttrs = CompatAttributesModel()
    private var resourceSchemaUrl: String? = null

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        resourceSchemaUrl = schemaUrl
        resourceAttrs.apply(attributes)
    }

    override fun resource(map: Map<String, Any>) {
        resourceAttrs.apply { setAttributes(map) }
    }

    fun build(
        clock: Clock = this.clock,
        baseResource: Resource = ResourceAdapter(OtelJavaResource.builder().build()),
    ): MeterProvider {
        val resource = ResourceAdapter(
            OtelJavaResource.create(resourceAttrs.otelJavaAttributes(), resourceSchemaUrl)
        )
        val merged = baseResource.merge(resource)
        if (merged.attributes.isNotEmpty() || merged.schemaUrl != null) {
            val attrs = CompatAttributesModel().apply { setAttributes(merged.attributes) }.otelJavaAttributes()
            builder.setResource(OtelJavaResource.create(attrs, merged.schemaUrl))
        }
        builder.setClock(OtelJavaClockWrapper(clock))
        return MeterProviderAdapter(builder.build())
    }
}
