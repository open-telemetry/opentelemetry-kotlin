package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentVariable
import io.opentelemetry.kotlin.config.envar.tracing.SpanLimitEnvVarConfigProcessor
import io.opentelemetry.kotlin.init.config.SpanLimitConfig

internal class FakeSpanLimitEnvVarConfigProcessor : SpanLimitEnvVarConfigProcessor() {
    override val envVars: List<EnvVarName> = emptyList()

    override fun parse(rawValue: String?): Int? = null

    override fun process(
        entries: Map<EnvVarName, EnvironmentVariable<Int>>,
        defaultValue: SpanLimitConfig
    ): SpanLimitConfig = defaultValue
}
