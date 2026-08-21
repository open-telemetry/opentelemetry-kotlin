package io.opentelemetry.kotlin.config.envar.tracing

import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanLimitsEnvVarsTest {

    @Test
    fun `should read every limit`() {
        val env = mapOf(
            "OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT" to "1",
            "OTEL_SPAN_ATTRIBUTE_VALUE_LENGTH_LIMIT" to "2",
            "OTEL_SPAN_LINK_COUNT_LIMIT" to "3",
            "OTEL_SPAN_EVENT_COUNT_LIMIT" to "4",
            "OTEL_EVENT_ATTRIBUTE_COUNT_LIMIT" to "5",
            "OTEL_LINK_ATTRIBUTE_COUNT_LIMIT" to "6",
        )
        val behavior = toBehavior(env::get)
        assertEquals(
            SpanLimitsBehavior(
                attributeCountLimit = 1,
                attributeValueLengthLimit = 2,
                linkCountLimit = 3,
                eventCountLimit = 4,
                attributeCountPerEventLimit = 5,
                attributeCountPerLinkLimit = 6,
            ),
            behavior,
        )
    }

    @Test
    fun `should leave unset env vars unset`() {
        assertEquals(SpanLimitsBehavior(), toBehavior { null })
    }

    @Test
    fun `should preserve a limit of zero`() {
        val behavior = toBehavior { "0" }
        assertEquals(
            SpanLimitsBehavior(
                attributeCountLimit = 0,
                attributeValueLengthLimit = 0,
                linkCountLimit = 0,
                eventCountLimit = 0,
                attributeCountPerEventLimit = 0,
                attributeCountPerLinkLimit = 0,
            ),
            behavior,
        )
    }

    @Test
    fun `should leave invalid env vars unset`() {
        INVALID_VALUES.forEach { rawValue ->
            val behavior = toBehavior { rawValue }
            assertEquals(
                SpanLimitsBehavior(),
                behavior,
                "<$rawValue> should not configure a limit",
            )
        }
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        SpanLimitsEnvVars(EnvVarReader(getEnvVar)).toBehavior()

    private companion object {
        val INVALID_VALUES = listOf("invalid", "", "-1", "2147483648")
    }
}
