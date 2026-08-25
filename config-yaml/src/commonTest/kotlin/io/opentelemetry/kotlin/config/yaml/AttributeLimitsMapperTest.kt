package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.config.schema.model.AttributeLimits
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AttributeLimitsMapperTest {

    @Test
    fun mapsEveryLimit() {
        val limits = AttributeLimits(attributeCountLimit = 64, attributeValueLengthLimit = 256)
        assertEquals(
            AttributeLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            limits.toBehavior(),
        )
    }

    @Test
    fun leavesOmittedLimitsUnset() {
        assertEquals(AttributeLimitsBehavior(), AttributeLimits().toBehavior())
    }

    @Test
    fun preservesALimitOfZero() {
        val behavior = AttributeLimits(attributeCountLimit = 0).toBehavior()
        assertEquals(0, behavior.attributeCountLimit)
    }

    @Test
    fun leavesLimitsTheSpecDisallowsUnset() {
        val invalid = listOf(-1L, Int.MAX_VALUE.toLong() + 1, Long.MAX_VALUE)
        invalid.forEach { value ->
            val behavior = AttributeLimits(
                attributeCountLimit = value,
                attributeValueLengthLimit = value,
            ).toBehavior()
            assertEquals(AttributeLimitsBehavior(), behavior, "<$value> should not configure a limit")
        }
    }
}
