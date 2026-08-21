package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import io.opentelemetry.kotlin.config.schema.model.LogRecordLimits
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogRecordLimitsMapperTest {

    @Test
    fun mapsEveryLimit() {
        val limits = LogRecordLimits(attributeCountLimit = 64, attributeValueLengthLimit = 256)
        assertEquals(
            LogLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            limits.toBehavior(),
        )
    }

    @Test
    fun leavesOmittedLimitsUnset() {
        assertEquals(LogLimitsBehavior(), LogRecordLimits().toBehavior())
    }

    @Test
    fun preservesALimitOfZero() {
        val behavior = LogRecordLimits(attributeCountLimit = 0).toBehavior()
        assertEquals(0, behavior.attributeCountLimit)
    }

    @Test
    fun leavesLimitsTheSpecDisallowsUnset() {
        val invalid = listOf(-1L, Int.MAX_VALUE.toLong() + 1, Long.MAX_VALUE)

        invalid.forEach { value ->
            val behavior = LogRecordLimits(
                attributeCountLimit = value,
                attributeValueLengthLimit = value,
            ).toBehavior()
            assertEquals(LogLimitsBehavior(), behavior, "<$value> should not configure a limit")
        }
    }
}
