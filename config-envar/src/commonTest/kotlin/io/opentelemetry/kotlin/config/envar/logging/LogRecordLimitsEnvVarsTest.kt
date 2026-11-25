package io.opentelemetry.kotlin.config.envar.logging

import io.opentelemetry.kotlin.behavior.logging.LogRecordLimitsBehavior
import io.opentelemetry.kotlin.config.envar.reader.reportingEnvVarReader
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogRecordLimitsEnvVarsTest {

    @Test
    fun `should read every limit`() {
        val env = mapOf(
            LogRecordLimitsEnvVars.ATTRIBUTE_COUNT_LIMIT to "64",
            LogRecordLimitsEnvVars.ATTRIBUTE_VALUE_LENGTH_LIMIT to "256",
        )
        val behavior = toBehavior(env::get)
        assertEquals(
            LogRecordLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            behavior,
        )
    }

    @Test
    fun `should leave unset env vars unset`() {
        assertEquals(LogRecordLimitsBehavior(), toBehavior { null })
    }

    @Test
    fun `should preserve a limit of zero`() {
        val behavior = toBehavior { "0" }
        assertEquals(
            LogRecordLimitsBehavior(attributeCountLimit = 0, attributeValueLengthLimit = 0),
            behavior,
        )
    }

    @Test
    fun `should leave invalid env vars unset`() {
        INVALID_VALUES.forEach { rawValue ->
            val behavior = toBehavior { rawValue }
            assertEquals(
                LogRecordLimitsBehavior(),
                behavior,
                "<$rawValue> should not configure a limit",
            )
        }
    }

    private fun toBehavior(getEnvVar: (String) -> String?) =
        LogRecordLimitsEnvVars(reportingEnvVarReader(getEnvVar)).toBehavior()

    private companion object {
        val INVALID_VALUES = listOf("invalid", "", "-1", "2147483648")
    }
}
