package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import io.opentelemetry.kotlin.config.envar.processor.EnvVarConfigProcessor
import io.opentelemetry.kotlin.init.config.LogLimitConfig

/**
 * Configures LogRecord Limits based on env vars
 */
internal sealed class LogLimitEnvVarConfigProcessor :
    EnvVarConfigProcessor<LogLimitConfig, Int>() {
    /**
     * specific list of env vars for log record limits
     */
    override val envVars: List<EnvVarName>
        get() = listOf(
            envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT"),
            envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")
        )
}

