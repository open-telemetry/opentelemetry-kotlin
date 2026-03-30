package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProviderBuilder
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.logging.LoggerProviderAdapter
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.OtelJavaLogRecordProcessorAdapter
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.semconv.ServiceAttributes

@ExperimentalApi
internal class CompatLoggerProviderConfig(
    private val clock: Clock,
) : LoggerProviderConfigDsl {

    private val builder: OtelJavaSdkLoggerProviderBuilder = OtelJavaSdkLoggerProvider.builder()
    internal val logLimitsConfig = CompatLogLimitsConfig()
    private var logLimitsAction: (LogLimitsConfigDsl.() -> Unit)? = null
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

    override fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        val processor = LogExportConfigCompat(clock).action()
        builder.addLogRecordProcessor(OtelJavaLogRecordProcessorAdapter(processor))
    }

    override fun logLimits(action: LogLimitsConfigDsl.() -> Unit) {
        logLimitsAction = action
    }

    fun build(
        clock: Clock,
        baseResource: Resource = ResourceAdapter(OtelJavaResource.builder().build()),
        globalLimits: CompatAttributeLimitsConfig? = null,
    ): LoggerProvider {
        if (globalLimits?.attributeCountLimitSet == true) {
            logLimitsConfig.attributeCountLimit = globalLimits.attributeCountLimit
        }
        if (globalLimits?.attributeValueLengthLimitSet == true) {
            logLimitsConfig.attributeValueLengthLimit = globalLimits.attributeValueLengthLimit
        }
        logLimitsAction?.invoke(logLimitsConfig)
        builder.setLogLimits(logLimitsConfig::build)
        val resource = ResourceAdapter(
            OtelJavaResource.create(resourceAttrs.otelJavaAttributes(), resourceSchemaUrl)
        )
        val merged = baseResource.merge(resource)
        if (merged.attributes.isNotEmpty() || merged.schemaUrl != null) {
            val attrs = CompatAttributesModel().apply { setAttributes(merged.attributes) }.otelJavaAttributes()
            builder.setResource(OtelJavaResource.create(attrs, merged.schemaUrl))
        }
        builder.setClock(OtelJavaClockWrapper(clock))
        return LoggerProviderAdapter(builder.build())
    }

    private class LogExportConfigCompat(override val clock: Clock) : LogExportConfigDsl
}
