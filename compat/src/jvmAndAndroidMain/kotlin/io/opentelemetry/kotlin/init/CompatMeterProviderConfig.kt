package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setTypedAttributes
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.metrics.MeterProvider
import io.opentelemetry.kotlin.metrics.MeterProviderAdapter
import io.opentelemetry.kotlin.semconv.ServiceAttributes

@ExperimentalApi
internal class CompatMeterProviderConfig(
    private val sdkErrorHandler: SdkErrorHandler,
) : MeterProviderConfigDsl {

    override var serviceName: String? = null
        set(value) {
            field = value
            value?.let { resourceAttrs.setStringAttribute(ServiceAttributes.SERVICE_NAME, it) }
        }

    private val resourceAttrs = CompatAttributesModel()
    private var resourceSchemaUrl: String? = null

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        resourceSchemaUrl = schemaUrl
        resourceAttrs.apply(attributes)
    }

    override fun resource(map: Map<String, Any>) {
        resourceAttrs.apply { setTypedAttributes(map) }
    }

    /**
     * Return a noop provider for now.
     */
    fun build(): MeterProvider = MeterProviderAdapter(OtelJavaMeterProvider.noop(), sdkErrorHandler)
}
