package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.propagation.CompatPropagatorConfigImpl
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.semconv.ServiceAttributes

@ExperimentalApi
internal class CompatOpenTelemetryConfig(
    clock: Clock,
) : OpenTelemetryConfigDsl {

    internal val tracerProviderConfig = CompatTracerProviderConfig(clock)
    internal val loggerProviderConfig = CompatLoggerProviderConfig(clock)
    internal val meterProviderConfig = CompatMeterProviderConfig(clock)
    internal val globalAttributeLimits = CompatAttributeLimitsConfig()
    internal val propagatorCfg = CompatPropagatorConfigImpl()

    private var customIdGenerator: (() -> IdGenerator)? = null

    override fun attributeLimits(action: AttributeLimitsConfigDsl.() -> Unit) {
        globalAttributeLimits.action()
    }

    private val globalResourceAttrs = CompatAttributesModel()
    private var globalResourceSchemaUrl: String? = null
    private var serviceNameOverride: String? = null

    override var serviceName: String
        get() = serviceNameOverride ?: "unknown_service"
        set(value) {
            serviceNameOverride = value
            globalResourceAttrs.setStringAttribute(ServiceAttributes.SERVICE_NAME, value)
        }

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

    override fun meterProvider(action: MeterProviderConfigDsl.() -> Unit) {
        meterProviderConfig.action()
    }

    override fun propagator(action: PropagatorConfigDsl.() -> TextMapPropagator) {
        propagatorCfg.action()
    }

    override fun idGenerator(action: () -> IdGenerator) {
        customIdGenerator = action
    }

    internal fun resolveIdGenerator(): IdGenerator = customIdGenerator?.invoke() ?: CompatIdGenerator()
}
