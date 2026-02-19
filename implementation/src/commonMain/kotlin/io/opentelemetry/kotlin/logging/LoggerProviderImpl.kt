package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainer
import io.opentelemetry.kotlin.export.DelegatingTelemetryCloseable
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.logging.export.createCompositeLogRecordProcessor
import io.opentelemetry.kotlin.provider.ApiProviderImpl

@OptIn(ExperimentalApi::class)
internal class LoggerProviderImpl(
    private val clock: Clock,
    loggingConfig: LoggingConfig,
    sdkFactory: SdkFactory,
    private val closeable: DelegatingTelemetryCloseable = DelegatingTelemetryCloseable()
) : LoggerProvider, TelemetryCloseable by closeable {

    private val apiProvider by lazy {
        ApiProviderImpl<Logger> { key ->
            @Suppress("DEPRECATION")
            val processor = when {
                loggingConfig.processors.isEmpty() -> null
                else -> createCompositeLogRecordProcessor(
                    loggingConfig.processors
                )
            }
            processor?.let(closeable::add)
            LoggerImpl(
                clock,
                processor,
                sdkFactory,
                key,
                loggingConfig.resource,
                loggingConfig.logLimits
            )
        }
    }

    override fun getLogger(
        name: String,
        version: String?,
        schemaUrl: String?,
        attributes: (MutableAttributeContainer.() -> Unit)?
    ): Logger {
        val key = apiProvider.createInstrumentationScopeInfo(name, version, schemaUrl, attributes)
        return apiProvider.getOrCreate(key)
    }
}
