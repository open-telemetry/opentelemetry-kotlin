package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class AttributeLimitsBehaviorTest {

    @Test
    fun everyFieldStartsUnset() {
        val behavior = AttributeLimitsBehavior()

        assertNull(behavior.attributeCountLimit)
        assertNull(behavior.attributeValueLengthLimit)
    }

    @Test
    fun higherLayerWinsAndLowerFillsTheGaps() {
        val merged = AttributeLimitsBehavior(attributeCountLimit = 1, attributeValueLengthLimit = 2)
            .mergeWith(AttributeLimitsBehavior(attributeValueLengthLimit = 99))

        assertEquals(1, merged.attributeCountLimit)
        assertEquals(99, merged.attributeValueLengthLimit)
    }

    @Test
    fun treatsZeroAsAConfiguredValue() {
        val merged = AttributeLimitsBehavior(attributeCountLimit = 128)
            .mergeWith(AttributeLimitsBehavior(attributeCountLimit = 0))

        assertEquals(0, merged.attributeCountLimit)
    }
}
