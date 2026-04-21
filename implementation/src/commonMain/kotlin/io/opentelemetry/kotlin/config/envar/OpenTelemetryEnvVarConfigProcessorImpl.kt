package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.config.envar.model.EnvironmentConfiguration
import io.opentelemetry.kotlin.init.config.LoggingConfig

/**
 * Retrieves environment configuration and initiates library configuration based on that
 */
internal class OpenTelemetryEnvVarConfigProcessorImpl(
    private val loggingConfig: LoggingConfig,
    private val logLimitProcessor: LogLimitEnvVarConfigProcessor
) : OpenTelemetryEnvVarConfigProcessor {
    override fun process(): EnvironmentConfiguration {
        return EnvironmentConfiguration(
            logLimitConfig = logLimitProcessor.resolve(
                defaultValue = loggingConfig.logLimits
            ) { envVar -> getEnvVarValue(envVar) }
        )
    }
}

internal expect fun getEnvVarValue(envVar: String): String?
