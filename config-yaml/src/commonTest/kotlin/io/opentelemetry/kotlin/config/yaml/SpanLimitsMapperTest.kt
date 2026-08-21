package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.config.schema.model.SpanLimits
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanLimitsMapperTest {

    @Test
    fun mapsEveryLimit() {
        val limits = SpanLimits(
            attributeCountLimit = 1,
            attributeValueLengthLimit = 2,
            linkCountLimit = 3,
            eventCountLimit = 4,
            eventAttributeCountLimit = 5,
            linkAttributeCountLimit = 6,
        )
        assertEquals(
            SpanLimitsBehavior(
                attributeCountLimit = 1,
                attributeValueLengthLimit = 2,
                linkCountLimit = 3,
                eventCountLimit = 4,
                attributeCountPerEventLimit = 5,
                attributeCountPerLinkLimit = 6,
            ),
            limits.toBehavior(),
        )
    }

    @Test
    fun leavesOmittedLimitsUnset() {
        assertEquals(SpanLimitsBehavior(), SpanLimits().toBehavior())
    }

    @Test
    fun preservesALimitOfZero() {
        val behavior = SpanLimits(eventCountLimit = 0).toBehavior()
        assertEquals(0, behavior.eventCountLimit)
    }

    @Test
    fun leavesLimitsTheSpecDisallowsUnset() {
        val invalid = listOf(-1L, Int.MAX_VALUE.toLong() + 1, Long.MAX_VALUE)
        invalid.forEach { value ->
            val behavior = SpanLimits(
                attributeCountLimit = value,
                attributeValueLengthLimit = value,
                linkCountLimit = value,
                eventCountLimit = value,
                eventAttributeCountLimit = value,
                linkAttributeCountLimit = value,
            ).toBehavior()
            assertEquals(SpanLimitsBehavior(), behavior, "<$value> should not configure a limit")
        }
    }
}
