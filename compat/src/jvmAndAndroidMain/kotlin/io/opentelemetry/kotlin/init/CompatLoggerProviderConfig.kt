package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProviderBuilder
import io.opentelemetry.kotlin.attributes.CompatMutableAttributeContainer
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.attributes.setAttributes
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.logging.LoggerProviderAdapter
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.OtelJavaLogRecordProcessorAdapter

@ExperimentalApi
internal class CompatLoggerProviderConfig(
    private val clock: Clock,
) : LoggerProviderConfigDsl {

    private val builder: OtelJavaSdkLoggerProviderBuilder = OtelJavaSdkLoggerProvider.builder()

    override fun resource(schemaUrl: String?, attributes: MutableAttributeContainer.() -> Unit) {
        val attrs = CompatMutableAttributeContainer().apply(attributes).otelJavaAttributes()
        builder.setResource(OtelJavaResource.create(attrs, schemaUrl))
    }

    override fun resource(map: Map<String, Any>) {
        resource {
            setAttributes(map)
        }
    }

    @Deprecated("Deprecated.", replaceWith = ReplaceWith("export {processor}"))
    override fun addLogRecordProcessor(processor: LogRecordProcessor) {
        builder.addLogRecordProcessor(OtelJavaLogRecordProcessorAdapter(processor))
    }

    override fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        val processor = LogExportConfigCompat(clock).action()
        @Suppress("DEPRECATION")
        addLogRecordProcessor(processor)
    }

    override fun logLimits(action: LogLimitsConfigDsl.() -> Unit) {
        builder.setLogLimits { CompatLogLimitsConfig().apply(action).build() }
    }

    fun build(clock: Clock): LoggerProvider {
        builder.setClock(OtelJavaClockWrapper(clock))
        return LoggerProviderAdapter(builder.build())
    }

    private class LogExportConfigCompat(override val clock: Clock) : LogExportConfigDsl
}
