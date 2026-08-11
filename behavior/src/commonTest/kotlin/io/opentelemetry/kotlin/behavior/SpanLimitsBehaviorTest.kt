package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanLimitsBehaviorTest {

    @Test
    fun everyFieldStartsUnset() {
        val behavior = SpanLimitsBehavior()

        assertNull(behavior.attributeCountLimit)
        assertNull(behavior.attributeValueLengthLimit)
        assertNull(behavior.linkCountLimit)
        assertNull(behavior.eventCountLimit)
        assertNull(behavior.attributeCountPerEventLimit)
        assertNull(behavior.attributeCountPerLinkLimit)
    }

    @Test
    fun keepsLowerValueWhenHigherLeavesItUnset() {
        val merged = SpanLimitsBehavior(attributeCountLimit = 10).mergeWith(SpanLimitsBehavior())

        assertEquals(10, merged.attributeCountLimit)
        assertNull(merged.attributeValueLengthLimit)
    }

    @Test
    fun adoptsHigherValueWhenLowerLeavesItUnset() {
        val merged = SpanLimitsBehavior().mergeWith(SpanLimitsBehavior(attributeCountLimit = 10))

        assertEquals(10, merged.attributeCountLimit)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredIt() {
        assertEquals(SpanLimitsBehavior(), SpanLimitsBehavior().mergeWith(SpanLimitsBehavior()))
    }

    @Test
    fun treatsZeroAsAConfiguredValue() {
        val merged = SpanLimitsBehavior(linkCountLimit = 128).mergeWith(SpanLimitsBehavior(linkCountLimit = 0))

        assertEquals(0, merged.linkCountLimit)
    }

    @Test
    fun refinesRatherThanReplaces() {
        val lower = SpanLimitsBehavior(
            attributeCountLimit = 1,
            attributeValueLengthLimit = 2,
            linkCountLimit = 3,
            eventCountLimit = 4,
            attributeCountPerEventLimit = 5,
            attributeCountPerLinkLimit = 6,
        )

        val merged = lower.mergeWith(SpanLimitsBehavior(linkCountLimit = 99))

        assertEquals(99, merged.linkCountLimit)
        assertEquals(1, merged.attributeCountLimit)
        assertEquals(2, merged.attributeValueLengthLimit)
        assertEquals(4, merged.eventCountLimit)
        assertEquals(5, merged.attributeCountPerEventLimit)
        assertEquals(6, merged.attributeCountPerLinkLimit)
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = SpanLimitsBehavior(
            attributeCountLimit = 1,
            attributeValueLengthLimit = 2,
            linkCountLimit = 3,
            eventCountLimit = 4,
            attributeCountPerEventLimit = 5,
            attributeCountPerLinkLimit = 6,
        )
        val higher = SpanLimitsBehavior(
            attributeCountLimit = 10,
            attributeValueLengthLimit = 20,
            linkCountLimit = 30,
            eventCountLimit = 40,
            attributeCountPerEventLimit = 50,
            attributeCountPerLinkLimit = 60,
        )

        assertEquals(higher, lower.mergeWith(higher))
    }

    @Test
    fun doesNotMutateEitherLayer() {
        val lower = SpanLimitsBehavior(attributeCountLimit = 1)
        val higher = SpanLimitsBehavior(linkCountLimit = 2)

        lower.mergeWith(higher)

        assertEquals(SpanLimitsBehavior(attributeCountLimit = 1), lower)
        assertEquals(SpanLimitsBehavior(linkCountLimit = 2), higher)
    }
}
