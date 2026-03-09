package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor

internal class LoggerProviderConfigImpl(
    private val clock: Clock,
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : LoggerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private val processors: MutableList<LogRecordProcessor> = mutableListOf()
    private val logLimitsConfigImpl = LogLimitsConfigImpl()

    override fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        require(processors.isEmpty()) { "export() should only be called once." }
        val processor = LogExportConfigImpl(clock).action()
        processors.add(processor)
    }

    override fun logLimits(action: LogLimitsConfigDsl.() -> Unit) {
        logLimitsConfigImpl.action()
    }

    fun generateLoggingConfig(): LoggingConfig = LoggingConfig(
        processors = processors.toList(),
        logLimits = generateLogLimitsConfig(),
        resource = resourceConfigImpl.generateResource(),
    )

    private fun generateLogLimitsConfig(): LogLimitConfig = LogLimitConfig(
        attributeCountLimit = logLimitsConfigImpl.attributeCountLimit,
        attributeValueLengthLimit = logLimitsConfigImpl.attributeValueLengthLimit,
    )
}
