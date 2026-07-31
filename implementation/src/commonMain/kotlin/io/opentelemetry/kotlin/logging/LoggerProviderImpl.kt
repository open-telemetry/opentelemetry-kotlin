package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.CompositeTelemetryCloseable
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.export.runWithTimeout
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.provider.ApiProviderImpl

internal class LoggerProviderImpl(
    private val clock: Clock,
    loggingConfig: LoggingConfig,
    contextFactory: ContextFactory,
    spanContextFactory: SpanContextFactory,
) : LoggerProvider, TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val sdkErrorHandler = loggingConfig.sdkErrorHandler
    private val closeable: TelemetryCloseable = CompositeTelemetryCloseable(
        loggingConfig.processor?.let { listOf(it) } ?: emptyList(),
        loggingConfig.sdkErrorHandler,
    )
    private val noopLogger = NoopOpenTelemetry.loggerProvider.getLogger("")

    private val apiProvider by lazy {
        ApiProviderImpl { key ->
            val loggerConfig = loggingConfig.loggerConfigurator.loggerConfig(key)
            if (!loggerConfig.enabled) {
                noopLogger
            } else {
                LoggerImpl(
                    clock,
                    loggingConfig.processor,
                    contextFactory,
                    spanContextFactory,
                    key,
                    loggingConfig.resource,
                    loggingConfig.logLimits,
                    shutdownState,
                    loggerConfig,
                )
            }
        }
    }

    override fun getLogger(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Logger =
        shutdownState.ifActiveOrElse(noopLogger) {
            if (name.isEmpty()) {
                sdkErrorHandler.onError(
                    SdkError.ApiMisuse(
                        api = "LoggerProvider.getLogger",
                        message = "Logger requested without instrumentation scope name",
                        severity = SdkErrorSeverity.WARNING,
                    )
                )
            }
            val key = apiProvider.createInstrumentationScopeInfo(name, version, schemaUrl, attributes)
            apiProvider.getOrCreate(key)
        }

    override suspend fun forceFlush(): OperationResultCode =
        runWithTimeout(BatchTelemetryDefaults.FORCE_FLUSH_TIMEOUT_MS, closeable::forceFlush)

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown(BatchTelemetryDefaults.SHUTDOWN_TIMEOUT_MS, closeable::shutdown)
}
