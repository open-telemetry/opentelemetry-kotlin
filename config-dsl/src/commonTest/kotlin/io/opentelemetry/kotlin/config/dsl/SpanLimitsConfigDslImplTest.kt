package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanLimitsConfigDslImplTest {

    @Test
    fun everyLimitStartsUnset() {
        assertEquals(SpanLimitsBehavior(), SpanLimitsConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsEveryConfiguredLimit() {
        val dsl = SpanLimitsConfigDslImpl().apply {
            attributeCountLimit = 1
            attributeValueLengthLimit = 2
            linkCountLimit = 3
            eventCountLimit = 4
            attributeCountPerEventLimit = 5
            attributeCountPerLinkLimit = 6
        }

        assertEquals(
            SpanLimitsBehavior(
                attributeCountLimit = 1,
                attributeValueLengthLimit = 2,
                linkCountLimit = 3,
                eventCountLimit = 4,
                attributeCountPerEventLimit = 5,
                attributeCountPerLinkLimit = 6,
            ),
            dsl.toBehavior(),
        )
    }

    @Test
    fun preservesALimitOfZero() {
        val dsl = SpanLimitsConfigDslImpl().apply { eventCountLimit = 0 }
        assertEquals(0, dsl.toBehavior().eventCountLimit)
    }

    @Test
    fun leavesNegativeLimitsUnset() {
        val dsl = SpanLimitsConfigDslImpl().apply {
            attributeCountLimit = -1
            attributeValueLengthLimit = -1
            linkCountLimit = -1
            eventCountLimit = -1
            attributeCountPerEventLimit = -1
            attributeCountPerLinkLimit = -1
        }
        assertEquals(SpanLimitsBehavior(), dsl.toBehavior())
    }
}
