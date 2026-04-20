package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessorImpl
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentConfiguration

/**
 * Retrieves environment configuration and initiates library configuration based on that
 */
internal class OpenTelemetryEnvVarConfigProcessorImpl(
    private val logLimitProcessor: LogLimitEnvVarConfigProcessor = LogLimitEnvVarConfigProcessorImpl(
        envVars = listOf(
            envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT"),
            envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")
        )
    )
) : OpenTelemetryEnvVarConfigProcessor {
    override fun process(): EnvironmentConfiguration {
        return EnvironmentConfiguration(
            logLimitConfig = logLimitProcessor.resolve { getEnvVarValue(it) }
        )
    }
}

internal expect fun getEnvVarValue(envVar: String): String?

