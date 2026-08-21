package io.opentelemetry.kotlin.config.envar

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AttributeLimitsEnvVarsTest {

    @Test
    fun `should read every limit`() {
        val env = mapOf(
            "OTEL_ATTRIBUTE_COUNT_LIMIT" to "64",
            "OTEL_ATTRIBUTE_VALUE_LENGTH_LIMIT" to "256",
        )
        val behavior = toBehavior(env::get)
        assertEquals(
            AttributeLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            behavior,
        )
    }

    @Test
    fun `should leave unset env vars unset`() {
        assertEquals(AttributeLimitsBehavior(), toBehavior { null })
    }

    @Test
    fun `should preserve a limit of zero`() {
        val behavior = toBehavior { "0" }
        assertEquals(
            AttributeLimitsBehavior(attributeCountLimit = 0, attributeValueLengthLimit = 0),
            behavior,
        )
    }

    @Test
    fun `should leave invalid env vars unset`() {
        INVALID_VALUES.forEach { rawValue ->
            val behavior = toBehavior { rawValue }
            assertEquals(
                AttributeLimitsBehavior(),
                behavior,
                "<$rawValue> should not configure a limit",
            )
        }
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        AttributeLimitsEnvVars(EnvVarReader(getEnvVar)).toBehavior()

    private companion object {
        val INVALID_VALUES = listOf("invalid", "", "-1", "2147483648")
    }
}
