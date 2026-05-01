package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentVariable
import io.opentelemetry.kotlin.init.config.LogLimitConfig

/**
 * Configures LogRecord Limits based on env vars
 */
internal class LogLimitEnvVarConfigProcessorImpl(
    override val envVars: List<EnvVarName>
) : LogLimitEnvVarConfigProcessor() {
    override fun parse(rawValue: String?): Int? = rawValue?.toInt()

    override fun process(
        entries: Map<EnvVarName, EnvironmentVariable<Int>>,
        defaultValue: LogLimitConfig
    ): LogLimitConfig {
        return LogLimitConfig(
            attributeCountLimit = entries[envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT")]?.value
                ?: defaultValue.attributeCountLimit,
            attributeValueLengthLimit = entries[envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")]?.value
                ?: defaultValue.attributeValueLengthLimit
        )
    }
}
