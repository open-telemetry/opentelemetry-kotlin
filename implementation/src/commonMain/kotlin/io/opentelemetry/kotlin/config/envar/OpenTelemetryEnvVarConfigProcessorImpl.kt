package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessorImpl
import io.opentelemetry.kotlin.config.envar.model.EnvironmentConfiguration

/**
 * Retrieves environment configuration and initiates library configuration based on that
 */
internal class OpenTelemetryEnvVarConfigProcessorImpl(
    private val logLimitProcessor: LogLimitEnvVarConfigProcessor = LogLimitEnvVarConfigProcessorImpl()
) : OpenTelemetryEnvVarConfigProcessor {
    override fun process(): EnvironmentConfiguration {
        return EnvironmentConfiguration(
            logLimitConfig = logLimitProcessor.resolve { getEnvVarValue(it) }
        )
    }
}

internal expect fun getEnvVarValue(envVar: String): String?

