package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.COMPAT_DEFAULT_TIMEOUT_MS
import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaClock
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setAttributeMap
import io.opentelemetry.kotlin.behavior.IdGeneratorBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.ResourceBehavior
import io.opentelemetry.kotlin.behavior.SeverityLevel
import io.opentelemetry.kotlin.behavior.toMinimumSdkErrorSeverity
import io.opentelemetry.kotlin.behavior.toSeverityLevel
import io.opentelemetry.kotlin.clock.ClockAdapter
import io.opentelemetry.kotlin.config.DeclarativeBehaviorReader
import io.opentelemetry.kotlin.config.dsl.AttributeLimitsConfigDslImpl
import io.opentelemetry.kotlin.behavior.BehaviorSupplier
import io.opentelemetry.kotlin.config.dsl.ResourceDetectionConfigDslImpl
import io.opentelemetry.kotlin.config.toBehaviorReader
import io.opentelemetry.kotlin.config.validateOrUseDefault
import io.opentelemetry.kotlin.error.GuardedSdkErrorHandler
import io.opentelemetry.kotlin.error.PlatformSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.propagation.TextMapPropagator
import io.opentelemetry.kotlin.propagation.TextMapPropagatorAdapter
import io.opentelemetry.kotlin.propagation.guarded
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import kotlin.concurrent.Volatile

@OptIn(ExperimentalApi::class)
internal class CompatOpenTelemetryConfig : OpenTelemetryConfigDsl, BehaviorSupplier<OpenTelemetryBehavior> {

    @Volatile private var configuredErrorHandler: SdkErrorHandler = PlatformSdkErrorHandler

    /**
     * The lowest severity forwarded to the configured handler. Set by [logLevel], then replaced by the
     * resolved log level once every config layer has been read.
     */
    @Volatile internal var minimumErrorSeverity: SdkErrorSeverity = SdkErrorSeverity.INFO

    internal val sdkErrorHandler = GuardedSdkErrorHandler {
        if (it.severity >= minimumErrorSeverity) {
            configuredErrorHandler.onError(it)
        }
    }

    internal var shutdownTimeoutMs: Long = COMPAT_DEFAULT_TIMEOUT_MS
        private set

    override fun shutdownTimeout(timeoutMs: Long) {
        shutdownTimeoutMs = validateOrUseDefault(
            sdkErrorHandler = sdkErrorHandler,
            api = "OpenTelemetryConfigDsl.shutdownTimeout",
            configParameterName = "timeoutMs",
            value = timeoutMs,
            default = shutdownTimeoutMs,
        ) { it > 0 }
    }

    @Volatile internal var clock: Clock = ClockAdapter(OtelJavaClock.getDefault())
        private set

    /**
     * Export configs are evaluated eagerly, possibly before [clock] is called, so they receive a
     * forwarder that resolves the configured clock on each reading instead.
     */
    private val forwardingClock = Clock { clock.now() }

    internal val tracerProviderConfig = CompatTracerProviderConfig(forwardingClock, sdkErrorHandler)
    internal val loggerProviderConfig = CompatLoggerProviderConfig(forwardingClock, sdkErrorHandler)
    internal val meterProviderConfig = CompatMeterProviderConfig(sdkErrorHandler)
    private val globalAttributeLimits = AttributeLimitsConfigDslImpl()

    @Volatile private var propagatorSupplier: (() -> TextMapPropagator)? = null

    @Volatile private var idGeneratorBehavior: IdGeneratorBehavior? = null

    @Volatile internal var configFilePath: String? = null
        private set

    @Volatile internal var declarativeConfigReader: DeclarativeBehaviorReader? = null
        private set

    override fun configFile(reader: DeclarativeConfigReader, path: String?) {
        val behaviorReader = reader.toBehaviorReader(sdkErrorHandler) ?: return
        declarativeConfigReader = behaviorReader
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
        globalResourceAttrs.apply { setAttributeMap(map) }
    }

    internal val resourceDetectionConfig = ResourceDetectionConfigDslImpl()

    override fun resourceDetection(action: ResourceDetectionConfigDsl.() -> Unit) {
        resourceDetectionConfig.action()
    }

    /**
     * Compat mode always stores the current context in
     * opentelemetry-java's ContextStorage, so this configuration is ignored.
     */
    @Volatile internal var contextConfigured: Boolean = false
        private set

    override fun context(action: ContextConfigDsl.() -> Unit) {
        contextConfigured = true
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

    override fun propagator(action: () -> TextMapPropagator) {
        propagatorSupplier = action
    }

    /**
     * Invokes the supplier configured via [propagator], falling back to a noop propagator if none
     * was configured or the supplier throws. The returned propagator reports failures in user code
     * to [sdkErrorHandler] instead of throwing.
     */
    internal fun createPropagator(): TextMapPropagator {
        val noop = TextMapPropagatorAdapter(OtelJavaTextMapPropagator.noop())
        val supplier = propagatorSupplier ?: return noop
        return sdkErrorHandler.guardOrDefault(noop, "Failed to create TextMapPropagator, using noop") {
            supplier().guarded(sdkErrorHandler)
        }
    }

    override fun idGenerator(action: () -> IdGenerator) {
        idGeneratorBehavior = IdGeneratorBehavior.Custom(action)
    }

    override fun clock(clock: Clock) {
        this.clock = clock
    }

    override fun errorHandler(handler: SdkErrorHandler) {
        configuredErrorHandler = handler
    }

    @Volatile private var disabled: Boolean? = null

    @Volatile private var logLevel: SeverityLevel? = null

    override fun disabled(disabled: Boolean) {
        this.disabled = disabled
    }

    override fun logLevel(level: SeverityNumber) {
        val severityLevel = level.toSeverityLevel()
        if (severityLevel == null) {
            sdkErrorHandler.onError(
                SdkError.ApiMisuse(
                    api = "OpenTelemetryConfigDsl.logLevel",
                    message = "Ignoring log level $level; it has no severity.",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
            return
        }
        logLevel = severityLevel
        minimumErrorSeverity = severityLevel.toMinimumSdkErrorSeverity()
    }

    override fun toBehavior(): OpenTelemetryBehavior = OpenTelemetryBehavior(
        disabled = disabled,
        logLevel = logLevel,
        resource = ResourceBehavior(
            serviceName = serviceName,
            schemaUrl = globalResourceSchemaUrl,
            attributes = globalResourceAttrs.attributes.takeUnless { it.isEmpty() },
        ),
        attributeLimits = globalAttributeLimits.toBehavior(),
        tracerProvider = tracerProviderConfig.toBehavior().copy(idGenerator = idGeneratorBehavior),
        loggerProvider = loggerProviderConfig.toBehavior(),
    )
}
