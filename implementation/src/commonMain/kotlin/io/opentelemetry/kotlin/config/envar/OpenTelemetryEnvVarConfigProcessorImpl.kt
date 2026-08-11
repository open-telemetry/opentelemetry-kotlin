package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.config.envar.model.EnvironmentConfiguration
import io.opentelemetry.kotlin.config.envar.tracing.SpanLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.init.config.TracingConfig

/**
 * Retrieves environment configuration and initiates library configuration based on that
 */
internal class OpenTelemetryEnvVarConfigProcessorImpl(
    private val loggingConfig: LoggingConfig,
    private val logLimitProcessor: LogLimitEnvVarConfigProcessor,
    private val tracingConfig: TracingConfig,
    private val spanLimitProcessor: SpanLimitEnvVarConfigProcessor,
) : OpenTelemetryEnvVarConfigProcessor {
    override fun process(): EnvironmentConfiguration {
        return EnvironmentConfiguration(
            logLimitConfig = logLimitProcessor.configure(
                defaultValue = loggingConfig.logLimits
            ) { envVar -> getEnvVarValue(envVar) },
            spanLimitConfig = spanLimitProcessor.configure(
                defaultValue = tracingConfig.spanLimits
            ) { envVar -> getEnvVarValue(envVar) },
        )
    }
}

internal expect fun getEnvVarValue(envVar: String): String?
