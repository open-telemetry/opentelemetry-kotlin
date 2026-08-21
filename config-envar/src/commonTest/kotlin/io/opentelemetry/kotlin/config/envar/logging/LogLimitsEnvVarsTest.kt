package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.config.envar.EnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogLimitsEnvVarsTest {

    @Test
    fun `should read every limit`() {
        val env = mapOf(
            "OTEL_LOGRECORD_ATTRIBUTE_COUNT_LIMIT" to "64",
            "OTEL_LOGRECORD_ATTRIBUTE_VALUE_LENGTH_LIMIT" to "256",
        )
        val behavior = toBehavior(env::get)
        assertEquals(
            LogLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            behavior,
        )
    }

    @Test
    fun `should leave unset env vars unset`() {
        assertEquals(LogLimitsBehavior(), toBehavior { null })
    }

    @Test
    fun `should preserve a limit of zero`() {
        val behavior = toBehavior { "0" }
        assertEquals(
            LogLimitsBehavior(attributeCountLimit = 0, attributeValueLengthLimit = 0),
            behavior,
        )
    }

    @Test
    fun `should leave invalid env vars unset`() {
        INVALID_VALUES.forEach { rawValue ->
            val behavior = toBehavior { rawValue }
            assertEquals(
                LogLimitsBehavior(),
                behavior,
                "<$rawValue> should not configure a limit",
            )
        }
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        LogLimitsEnvVars(EnvVarReader(getEnvVar)).toBehavior()

    private companion object {
        val INVALID_VALUES = listOf("invalid", "", "-1", "2147483648")
    }
}
