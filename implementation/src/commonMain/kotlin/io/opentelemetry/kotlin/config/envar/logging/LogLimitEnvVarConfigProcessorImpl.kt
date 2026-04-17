package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentVariable
import io.opentelemetry.kotlin.init.config.LogLimitConfig

/**
 * Configures LogRecord Limits based on env vars
 */
internal class LogLimitEnvVarConfigProcessorImpl : LogLimitEnvVarConfigProcessor() {
    override fun parse(value: String?): Int? = value?.toInt()

    override fun process(entries: Map<EnvVarName, EnvironmentVariable<Int>>): LogLimitConfig {
        return LogLimitConfig(
            attributeCountLimit = entries[envVarName("OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT")]?.value
                ?: DEFAULT_ATTRIBUTE_LIMIT,
            attributeValueLengthLimit = entries[envVarName("OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT")]?.value
                ?: DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
        )
    }
}
