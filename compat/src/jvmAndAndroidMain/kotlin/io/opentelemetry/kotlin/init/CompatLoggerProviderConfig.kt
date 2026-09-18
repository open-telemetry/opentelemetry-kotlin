package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerConfig
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaScopeConfigurator
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProviderBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProviderUtil
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.setTypedAttributes
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.config.dsl.LogLimitsConfigDslImpl
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.logging.LoggerConfigurator
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.logging.LoggerProviderAdapter
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.OtelJavaLogRecordProcessorAdapter
import io.opentelemetry.kotlin.logging.toOtelJavaSeverityNumber
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.scope.toOtelKotlinInstrumentationScopeInfo
import io.opentelemetry.kotlin.semconv.ServiceAttributes

@ExperimentalApi
internal class CompatLoggerProviderConfig(
    private val clock: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
) : LoggerProviderConfigDsl {

    private val builder: OtelJavaSdkLoggerProviderBuilder = OtelJavaSdkLoggerProvider.builder()

    internal var logLimits: AttributeLimitsBehavior = AttributeLimitsBehavior()
        private set
    private var loggerConfigurator: LoggerConfigurator? = null
    private var hasProcessor = false
    override var serviceName: String? = null
        set(value) {
            field = value
            value?.let { resourceAttrs.setStringAttribute(ServiceAttributes.SERVICE_NAME, it) }
        }

    private val resourceAttrs = CompatAttributesModel()
    private var resourceSchemaUrl: String? = null
    private val logLimitsDsl = LogLimitsConfigDslImpl()

    override fun resource(schemaUrl: String?, attributes: AttributesMutator.() -> Unit) {
        resourceSchemaUrl = schemaUrl
        resourceAttrs.apply(attributes)
    }

    override fun resource(map: Map<String, Any>) {
        resourceAttrs.apply { setTypedAttributes(map) }
    }

    override fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        hasProcessor = true
        val processor = LogExportConfigCompat(clock, sdkErrorHandler).action()
        builder.addLogRecordProcessor(OtelJavaLogRecordProcessorAdapter(processor))
    }

    override fun logLimits(action: LogLimitsConfigDsl.() -> Unit) {
        logLimitsDsl.action()
    }

    override fun loggerConfigurator(configurator: LoggerConfigurator) {
        loggerConfigurator = configurator
    }

    private fun applyLoggerConfigurator(configurator: LoggerConfigurator) {
        val scopeConfigurator = OtelJavaScopeConfigurator<OtelJavaLoggerConfig> { javaScope ->
            val scope = javaScope.toOtelKotlinInstrumentationScopeInfo()
            val config = configurator.loggerConfig(scope)
            OtelJavaLoggerConfig.builder()
                .setEnabled(config.enabled)
                .setMinimumSeverity(config.minimumSeverity.toOtelJavaSeverityNumber())
                .setTraceBased(config.traceBased)
                .build()
        }
        OtelJavaSdkLoggerProviderUtil.setLoggerConfigurator(builder, scopeConfigurator)
    }

    fun build(
        clock: Clock,
        baseResource: Resource = ResourceAdapter(OtelJavaResource.builder().build()),
        logLimits: LogLimitsBehavior,
    ): LoggerProvider {
        this.logLimits = logLimits
        builder.setLogLimits { this.logLimits.toOtelJavaLogLimits() }
        loggerConfigurator?.let(::applyLoggerConfigurator)
        val resource = ResourceAdapter(
            OtelJavaResource.create(resourceAttrs.otelJavaAttributes(), resourceSchemaUrl)
        )
        val merged = baseResource.merge(resource)
        if (merged.attributes.isNotEmpty() || merged.schemaUrl != null) {
            val attrs = attrsFromMap(merged.attributes)
            builder.setResource(OtelJavaResource.create(attrs, merged.schemaUrl))
        }
        builder.setClock(OtelJavaClockWrapper(clock))
        return LoggerProviderAdapter(builder.build())
    }

    internal fun applyResolvedProcessor(behavior: LogRecordProcessorBehavior?) {
        if (hasProcessor || behavior == null) {
            return
        }
        // For now, we only support console exporter via the behavior
        if (behavior?.console != null) {
            hasProcessor = true
            val processor = LogExportConfigCompat(clock, sdkErrorHandler).action()
            builder.addLogRecordProcessor(OtelJavaLogRecordProcessorAdapter(processor))
        }
    }

    fun toBehavior(): LoggerProviderBehavior =
        LoggerProviderBehavior(
            logLimits = logLimitsDsl.toBehavior(),
            processor = if (hasProcessor) {
                LogRecordProcessorBehavior(console = ConsoleExporterBehavior())
            } else {
                null
            }
        )

    private class LogExportConfigCompat(
        override val clock: Clock,
        override val sdkErrorHandler: SdkErrorHandler,
    ) : LogExportConfigDsl
}
