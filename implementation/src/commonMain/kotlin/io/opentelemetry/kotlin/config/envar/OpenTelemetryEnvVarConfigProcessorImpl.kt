package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessorImpl
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentConfiguration
import io.opentelemetry.kotlin.init.OpenTelemetryConfigDsl
import io.opentelemetry.kotlin.init.OpenTelemetryConfigImpl
import io.opentelemetry.kotlin.init.config.LoggingConfig

@ExperimentalApi
internal fun createOpenTelemetryEnvVarConfigProcessor(
    clock: Clock,
    config: OpenTelemetryConfigDsl.() -> Unit
): OpenTelemetryEnvVarConfigProcessor {
    val cfg = OpenTelemetryConfigImpl(clock).apply(config)
    val logLimitProcessor = LogLimitEnvVarConfigProcessorImpl(
        envVars = logLimitEnvars()
    )
    return OpenTelemetryEnvVarConfigProcessorImpl(
        loggingConfig = cfg.generateLoggingConfig(),
        logLimitProcessor = logLimitProcessor
    )
}

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
            ) {
                getEnvVarValue(
                    it
                )
            }
        )
    }
}

internal fun logLimitEnvars() = listOf(
    envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT"),
    envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")
)

internal expect fun getEnvVarValue(envVar: String): String?

