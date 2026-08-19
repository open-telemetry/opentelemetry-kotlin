package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.config.envar.model.EnvVarName
import io.opentelemetry.kotlin.config.envar.model.EnvVarName.Companion.envVarName
import io.opentelemetry.kotlin.config.envar.model.EnvironmentVariable
import io.opentelemetry.kotlin.init.config.SpanLimitConfig

internal class SpanLimitEnvVarConfigProcessorImpl(
    override val envVars: List<EnvVarName>
) : SpanLimitEnvVarConfigProcessor() {
    override fun parse(rawValue: String?): Int? = rawValue?.toIntOrNull()?.takeIf { it >= 0 }

    override fun process(
        entries: Map<EnvVarName, EnvironmentVariable<Int>>,
        defaultValue: SpanLimitConfig
    ): SpanLimitConfig {
        return SpanLimitConfig(
            attributeCountLimit = entries[envVarName("OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT")]?.value
                ?: defaultValue.attributeCountLimit,
            attributeValueLengthLimit = entries[envVarName("OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT")]?.value
                ?: defaultValue.attributeValueLengthLimit,
            linkCountLimit = entries[envVarName("OTEL_SPAN_LINK_COUNT_LIMIT")]?.value
                ?: defaultValue.linkCountLimit,
            eventCountLimit = entries[envVarName("OTEL_SPAN_EVENT_COUNT_LIMIT")]?.value
                ?: defaultValue.eventCountLimit,
            attributeCountPerEventLimit = entries[envVarName("OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT")]?.value
                ?: defaultValue.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = entries[envVarName("OTEL_LINK_ATTRIBUTE_COUNT_LIMIT")]?.value
                ?: defaultValue.attributeCountPerLinkLimit,
        )
    }
}
