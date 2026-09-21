package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setTypedAttributes
import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.dsl.AttributeLimitsConfigDslImpl
import io.opentelemetry.kotlin.config.dsl.BehaviorSupplier
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.propagation.CompatPropagatorConfigImpl
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import kotlin.concurrent.Volatile

@ExperimentalApi
internal class CompatOpenTelemetryConfig(
    clock: Clock,
) : OpenTelemetryConfigDsl, BehaviorSupplier<OpenTelemetryBehavior> {

    @Volatile private var configuredErrorHandler: SdkErrorHandler = NoopSdkErrorHandler
    internal val sdkErrorHandler = GuardedSdkErrorHandler { configuredErrorHandler.onError(it) }

    internal val tracerProviderConfig = CompatTracerProviderConfig(clock, sdkErrorHandler)
    internal val loggerProviderConfig = CompatLoggerProviderConfig(clock, sdkErrorHandler)
    internal val meterProviderConfig = CompatMeterProviderConfig(clock)
    private val globalAttributeLimits = AttributeLimitsConfigDslImpl()
    internal val propagatorCfg = CompatPropagatorConfigImpl()

    @Volatile private var idGeneratorBehavior: IdGeneratorBehavior? = null

    @Volatile internal var configFilePath: String? = null
        private set

    override fun configFile(path: String) {
        configFilePath = path
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

    internal val resourceDetectionConfig = CompatResourceDetectionConfig()

    override fun resourceDetection(action: ResourceDetectionConfigDsl.() -> Unit) {
        resourceDetectionConfig.action()
    }

    /**
     * The resource declared via the DSL, before any detected attributes are merged in.
     */
    internal fun buildDeclaredResource(): Resource =
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
        idGeneratorBehavior = IdGeneratorBehavior.Custom(action)
    }

    override fun errorHandler(handler: SdkErrorHandler) {
        configuredErrorHandler = handler
    }

    override fun toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
        attributeLimits = globalAttributeLimits.toBehavior(),
        tracerProvider = tracerProviderConfig.toBehavior().copy(idGenerator = idGeneratorBehavior),
        loggerProvider = loggerProviderConfig.toBehavior(),
    )
}
