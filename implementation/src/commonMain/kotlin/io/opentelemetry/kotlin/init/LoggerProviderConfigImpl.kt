package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.logging.LoggerConfigImpl
import io.opentelemetry.kotlin.logging.LoggerConfigurator
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.resource.Resource

internal class LoggerProviderConfigImpl(
    private val clock: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val resourceConfigImpl: ResourceConfigImpl = ResourceConfigImpl()
) : LoggerProviderConfigDsl, ResourceConfigDsl by resourceConfigImpl {

    private var processor: LogRecordProcessor? = null
    private var logLimitsAction: LogLimitsConfigDsl.() -> Unit = {}
    private val defaultLoggerConfig = LoggerConfigImpl(true)
    private var loggerConfigurator: LoggerConfigurator = LoggerConfigurator {
        defaultLoggerConfig
    }

    override fun export(action: LogExportConfigDsl.() -> LogRecordProcessor) {
        if (processor != null) {
            sdkErrorHandler.reportError(
                SdkError.ApiMisuse(
                    api = "LoggerProviderConfigDsl.export",
                    message = "export() should only be called once.",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
            return
        }
        processor = LogExportConfigImpl(clock, sdkErrorHandler).action()
    }

    override fun logLimits(action: LogLimitsConfigDsl.() -> Unit) {
        logLimitsAction = action
    }

    override fun loggerConfigurator(configurator: LoggerConfigurator) {
        loggerConfigurator = configurator
    }

    fun generateLoggingConfig(
        base: Resource,
        globalLimits: AttributeLimitsConfigDsl? = null
    ): LoggingConfig = LoggingConfig(
        processor = processor,
        logLimits = generateLogLimitsConfig(globalLimits),
        resource = base.merge(resourceConfigImpl.generateResource()),
        sdkErrorHandler = sdkErrorHandler,
        loggerConfigurator = loggerConfigurator,
    )

    /**
     * A limit left unset by the log limits falls back to the global attribute limits, then to the
     * default this SDK applies.
     */
    private fun generateLogLimitsConfig(globalLimits: AttributeLimitsConfigDsl?): LogLimitConfig {
        val impl = LogLimitsConfigImpl()
        logLimitsAction(impl)
        return LogLimitConfig(
            attributeCountLimit = impl.attributeCountLimit
                ?: globalLimits?.attributeCountLimit
                ?: DEFAULT_ATTRIBUTE_LIMIT,
            attributeValueLengthLimit = impl.attributeValueLengthLimit
                ?: globalLimits?.attributeValueLengthLimit
                ?: DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT,
        )
    }
}
