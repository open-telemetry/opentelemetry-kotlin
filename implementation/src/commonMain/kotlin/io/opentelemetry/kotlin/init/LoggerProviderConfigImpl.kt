package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.config.dsl.ResourceConfigDslImpl
import io.opentelemetry.kotlin.config.dsl.logging.LogExportConfigDslImpl
import io.opentelemetry.kotlin.config.dsl.logging.LogLimitsConfigDslImpl
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.logging.LoggerConfigImpl
import io.opentelemetry.kotlin.logging.LoggerConfigurator
import io.opentelemetry.kotlin.logging.export.LogRecordProcessor
import io.opentelemetry.kotlin.logging.export.batchLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.stdoutLogRecordExporter
import io.opentelemetry.kotlin.resource.Resource

internal class LoggerProviderConfigImpl(
    private val clock: Clock,
    private val sdkErrorHandler: SdkErrorHandler,
    private val resourceConfig: ResourceConfigDslImpl = ResourceConfigDslImpl()
) : LoggerProviderConfigDsl, ResourceConfigDsl by resourceConfig {

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
        processor = LogExportConfigDslImpl(clock, sdkErrorHandler).action()
    }

    override fun logLimits(action: LogLimitsConfigDsl.() -> Unit) {
        logLimits.action()
    }

    override fun loggerConfigurator(configurator: LoggerConfigurator) {
        loggerConfigurator = configurator
    }

    fun generateLoggingConfig(
        base: Resource,
        processorBehavior: LogRecordProcessorBehavior? = null,
    ): LoggingConfig = LoggingConfig(
        processor = processor ?: processorFromBehavior(processorBehavior),
        resource = base.merge(resourceConfig.toBehavior().toResource()),
        sdkErrorHandler = sdkErrorHandler,
        loggerConfigurator = loggerConfigurator,
    )

    fun toBehavior(): LoggerProviderBehavior =
        LoggerProviderBehavior(
            logLimits = logLimits.toBehavior(),
        )

    private fun processorFromBehavior(processorBehavior: LogRecordProcessorBehavior?): LogRecordProcessor? {
        val simple = processorBehavior?.simple
        val batch = processorBehavior?.batch
        if (simple?.exporter?.console == null && batch?.exporter?.console == null) {
            return null
        }
        return LogExportConfigDslImpl(clock, sdkErrorHandler).run {
            when {
                batch != null -> batchLogRecordProcessor(
                    exporter = stdoutLogRecordExporter(),
                    scheduleDelayMs = batch.scheduleDelay ?: BatchTelemetryDefaults.LOG_SCHEDULE_DELAY_MS,
                    exportTimeoutMs = batch.exportTimeout ?: BatchTelemetryDefaults.EXPORT_TIMEOUT_MS,
                    maxQueueSize = batch.maxQueueSize ?: BatchTelemetryDefaults.MAX_QUEUE_SIZE,
                    maxExportBatchSize = batch.maxExportBatchSize ?: BatchTelemetryDefaults.MAX_EXPORT_BATCH_SIZE
                )
                simple != null -> simpleLogRecordProcessor(stdoutLogRecordExporter())
                else -> null
            }
        }
    }
}
