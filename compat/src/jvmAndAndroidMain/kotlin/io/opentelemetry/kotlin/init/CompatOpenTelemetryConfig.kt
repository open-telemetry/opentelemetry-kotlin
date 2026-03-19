package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter

@ExperimentalApi
internal class CompatOpenTelemetryConfig(
    clock: Clock,
    idGenerator: IdGenerator,
) : OpenTelemetryConfigDsl {

    internal val tracerProviderConfig = CompatTracerProviderConfig(clock, idGenerator)
    internal val loggerProviderConfig = CompatLoggerProviderConfig(clock)

    private val globalResourceAttrs = CompatAttributesModel()
    private var globalResourceSchemaUrl: String? = null

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        globalResourceSchemaUrl = schemaUrl
        globalResourceAttrs.apply(attributes)
    }

    override fun resource(map: Map<String, Any>) {
        globalResourceAttrs.apply { setAttributes(map) }
    }

    internal fun buildGlobalResource(): Resource =
        ResourceAdapter(OtelJavaResource.create(globalResourceAttrs.otelJavaAttributes(), globalResourceSchemaUrl))

    override fun context(action: ContextConfigDsl.() -> Unit) {
        // no-op
    }

    override fun tracerProvider(action: TracerProviderConfigDsl.() -> Unit) {
        tracerProviderConfig.action()
    }

    override fun loggerProvider(action: LoggerProviderConfigDsl.() -> Unit) {
        loggerProviderConfig.action()
    }
}
