package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setTypedAttributes
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.factory.CompatResourceFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.propagation.CompatPropagatorConfigImpl
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.resource.detectResource
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import kotlin.concurrent.Volatile

@ExperimentalApi
internal class CompatOpenTelemetryConfig(
    clock: Clock,
) : OpenTelemetryConfigDsl {

    @Volatile private var configuredErrorHandler: SdkErrorHandler = NoopSdkErrorHandler
    internal val sdkErrorHandler = GuardedSdkErrorHandler { configuredErrorHandler.onError(it) }

    internal val tracerProviderConfig = CompatTracerProviderConfig(clock, sdkErrorHandler)
    internal val loggerProviderConfig = CompatLoggerProviderConfig(clock, sdkErrorHandler)
    internal val meterProviderConfig = CompatMeterProviderConfig(clock)
    internal val globalAttributeLimits = CompatAttributeLimitsConfig()
    internal val propagatorCfg = CompatPropagatorConfigImpl()

    private var customIdGenerator: (() -> IdGenerator)? = null

    override fun configFile(path: String) {
        // no-op
    }

    override fun attributeLimits(action: AttributeLimitsConfigDsl.() -> Unit) {
        globalAttributeLimits.action()
    }

    private val globalResourceAttrs = CompatAttributesModel()
    private var globalResourceSchemaUrl: String? = null
    override var serviceName: String? = null
        set(value) {
            field = value
            value?.let { globalResourceAttrs.setStringAttribute(ServiceAttributes.SERVICE_NAME, it) }
        }

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        globalResourceSchemaUrl = schemaUrl
        globalResourceAttrs.apply(attributes)
    }

    override fun resource(map: Map<String, Any>) {
        globalResourceAttrs.apply { setTypedAttributes(map) }
    }

    private val resourceDetectionConfig = CompatResourceDetectionConfig()

    override fun resourceDetection(action: ResourceDetectionConfigDsl.() -> Unit) {
        resourceDetectionConfig.action()
    }

    internal fun buildGlobalResource(): Resource {
        val declared =
            ResourceAdapter(OtelJavaResource.create(globalResourceAttrs.otelJavaAttributes(), globalResourceSchemaUrl))
        return resourceDetectionConfig.detectors.detectResource(CompatResourceFactory, sdkErrorHandler).merge(declared)
    }

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

    override fun errorHandler(handler: SdkErrorHandler) {
        configuredErrorHandler = handler
    }

    internal fun resolveIdGenerator(): IdGenerator = customIdGenerator?.invoke() ?: CompatIdGenerator()
}
