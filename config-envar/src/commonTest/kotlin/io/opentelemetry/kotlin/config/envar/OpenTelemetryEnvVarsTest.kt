package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.behavior.TracerProviderBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OpenTelemetryEnvVarsTest {

    @Test
    fun `should read every node from its own env vars`() {
        val env = mapOf(
            "OTEL_ATTRIBUTE_COUNT_LIMIT" to "1",
            "OTEL_ATTRIBUTE_VALUE_LENGTH_LIMIT" to "2",
            "OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT" to "3",
            "OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT" to "4",
            "OTEL_SPAN_LINK_COUNT_LIMIT" to "5",
            "OTEL_SPAN_EVENT_COUNT_LIMIT" to "6",
            "OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT" to "7",
            "OTEL_LINK_ATTRIBUTE_COUNT_LIMIT" to "8",
            "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT" to "9",
            "OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT" to "10",
        )

        val expected = OpenTelemetryBehavior(
            attributeLimits = AttributeLimitsBehavior(
                attributeCountLimit = 1,
                attributeValueLengthLimit = 2,
            ),
            tracerProvider = TracerProviderBehavior(
                spanLimits = SpanLimitsBehavior(
                    attributeCountLimit = 3,
                    attributeValueLengthLimit = 4,
                    linkCountLimit = 5,
                    eventCountLimit = 6,
                    attributeCountPerEventLimit = 7,
                    attributeCountPerLinkLimit = 8,
                ),
            ),
            loggerProvider = LoggerProviderBehavior(
                logLimits = LogLimitsBehavior(
                    attributeCountLimit = 9,
                    attributeValueLengthLimit = 10,
                ),
            ),
        )
        assertEquals(expected, toBehavior(env::get))
    }

    @Test
    fun `should leave every limit unset when the environment configures nothing`() {
        val expected = OpenTelemetryBehavior(
            attributeLimits = AttributeLimitsBehavior(),
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior()),
            loggerProvider = LoggerProviderBehavior(logLimits = LogLimitsBehavior()),
        )
        assertEquals(expected, toBehavior { null })
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        OpenTelemetryEnvVars(EnvVarReader(getEnvVar)).toBehavior()
}
