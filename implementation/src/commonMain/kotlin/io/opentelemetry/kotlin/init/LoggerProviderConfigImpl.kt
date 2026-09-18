package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.config.dsl.LogLimitsConfigDslImpl
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
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
    private val logLimits = LogLimitsConfigDslImpl()
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
        logLimits.action()
    }

    override fun loggerConfigurator(configurator: LoggerConfigurator) {
        loggerConfigurator = configurator
    }

    fun generateLoggingConfig(
        base: Resource,
        logLimits: LogLimitsBehavior,
    ): LoggingConfig = LoggingConfig(
        processor = processor,
        logLimits = logLimits,
        resource = base.merge(resourceConfigImpl.generateResource()),
        sdkErrorHandler = sdkErrorHandler,
        loggerConfigurator = loggerConfigurator,
    )

    internal fun applyResolvedProcessor(behavior: LogRecordProcessorBehavior?) {
        if (processor != null || behavior == null) {
            return
        }
        // For now, we only support console exporter via the behavior
        // TODO: Support other exporter types when their behaviors are added
        if (behavior?.console != null) {
            // Note: This doesn't actually create a functional processor yet
            // The full implementation requires adding processor factories
            // For now, this just acknowledges that console was requested
        }
    }

    fun toBehavior(): LoggerProviderBehavior =
        LoggerProviderBehavior(
            logLimits = logLimits.toBehavior(),
            processor = processor?.let { 
                LogRecordProcessorBehavior(
                    console = ConsoleExporterBehavior()
                )
            }
        )
}
