package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.resource.Resource

internal class LoggerProviderConfigImpl(
    private val clock: Clock,
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : LoggerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private var processor: LogRecordProcessor? = null
    private var logLimitsAction: LogLimitsConfigDsl.() -> Unit = {}

    override fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        require(processor == null) { "export() should only be called once." }
        processor = LogExportConfigImpl(clock).action()
    }

    override fun logLimits(action: LogLimitsConfigDsl.() -> Unit) {
        logLimitsAction = action
    }

    fun generateLoggingConfig(base: Resource, globalLimits: AttributeLimitsConfigImpl? = null): LoggingConfig = LoggingConfig(
        processor = processor,
        logLimits = generateLogLimitsConfig(globalLimits),
        resource = base.merge(resourceConfigImpl.generateResource()),
    )

    private fun generateLogLimitsConfig(globalLimits: AttributeLimitsConfigImpl?): LogLimitConfig {
        val impl = LogLimitsConfigImpl()
        globalLimits?.let {
            impl.attributeCountLimit = it.attributeCountLimit
            impl.attributeValueLengthLimit = it.attributeValueLengthLimit
        }
        logLimitsAction(impl)
        return LogLimitConfig(
            attributeCountLimit = impl.attributeCountLimit,
            attributeValueLengthLimit = impl.attributeValueLengthLimit,
        )
    }
}
