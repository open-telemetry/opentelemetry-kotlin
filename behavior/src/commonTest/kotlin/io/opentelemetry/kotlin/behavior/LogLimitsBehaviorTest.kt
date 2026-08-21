package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogLimitsBehaviorTest {

    @Test
    fun everyFieldStartsUnset() {
        val behavior = LogLimitsBehavior()

        assertNull(behavior.attributeCountLimit)
        assertNull(behavior.attributeValueLengthLimit)
    }

    @Test
    fun higherLayerWinsAndLowerFillsTheGaps() {
        val merged = LogLimitsBehavior(attributeCountLimit = 1, attributeValueLengthLimit = 2)
            .mergeWith(LogLimitsBehavior(attributeValueLengthLimit = 99))

        assertEquals(1, merged.attributeCountLimit)
        assertEquals(99, merged.attributeValueLengthLimit)
    }

    @Test
    fun treatsZeroAsAConfiguredValue() {
        val merged = LogLimitsBehavior(attributeCountLimit = 128)
            .mergeWith(LogLimitsBehavior(attributeCountLimit = 0))

        assertEquals(0, merged.attributeCountLimit)
    }
}
