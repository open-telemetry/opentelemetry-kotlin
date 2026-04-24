package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.logging.LogLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentVariable
import io.opentelemetry.kotlin.init.config.LogLimitConfig

internal class FakeLogLimitEnvVarConfigProcessor :
    LogLimitEnvVarConfigProcessor() {
    override val envVars: List<EnvVarName> = emptyList()

    override fun parse(value: String?): Int? = null

    override fun process(
        entries: Map<EnvVarName, EnvironmentVariable<Int>>,
        defaultValue: LogLimitConfig
    ): LogLimitConfig = defaultValue
}