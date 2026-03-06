package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.export.DelegatingTelemetryCloseable
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.provider.ApiProviderImpl

internal class LoggerProviderImpl(
    private val clock: Clock,
    loggingConfig: LoggingConfig,
    contextFactory: ContextFactory,
    spanContextFactory: SpanContextFactory,
    spanFactory: SpanFactory,
    private val closeable: DelegatingTelemetryCloseable = DelegatingTelemetryCloseable()
) : LoggerProvider, TelemetryCloseable {

    private val shutdownState: MutableShutdownState = MutableShutdownState()
    private val noopLogger = NoopOpenTelemetry.loggerProvider.getLogger("")

    private val apiProvider by lazy {
        ApiProviderImpl<Logger> { key ->
            val processor = loggingConfig.processors.firstOrNull()
            processor?.let(closeable::add)
            LoggerImpl(
                clock,
                processor,
                contextFactory,
                spanContextFactory,
                spanFactory,
                key,
                loggingConfig.resource,
                loggingConfig.logLimits,
                shutdownState,
            )
        }
    }

    override fun getLogger(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (AttributesMutator.() -> Unit)?
    ): Logger =
        shutdownState.ifActiveOrElse(noopLogger) {
            val key = apiProvider.createInstrumentationScopeInfo(name, version, schemaUrl, attributes)
            apiProvider.getOrCreate(key)
        }

    override suspend fun forceFlush(): OperationResultCode = closeable.forceFlush()

    override suspend fun shutdown(): OperationResultCode =
        shutdownState.shutdown {
            closeable.shutdown()
        }
}
