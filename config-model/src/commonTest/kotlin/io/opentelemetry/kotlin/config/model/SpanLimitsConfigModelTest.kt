package io.opentelemetry.kotlin.config.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanLimitsConfigModelTest {

    @Test
    fun everyFieldStartsUnset() {
        val model = SpanLimitsConfigModel()

        assertNull(model.attributeCountLimit)
        assertNull(model.attributeValueLengthLimit)
        assertNull(model.linkCountLimit)
        assertNull(model.eventCountLimit)
        assertNull(model.attributeCountPerEventLimit)
        assertNull(model.attributeCountPerLinkLimit)
    }

    @Test
    fun keepsLowerValueWhenHigherLeavesItUnset() {
        val merged = SpanLimitsConfigModel(attributeCountLimit = 10).mergeWith(SpanLimitsConfigModel())

        assertEquals(10, merged.attributeCountLimit)
        assertNull(merged.attributeValueLengthLimit)
    }

    @Test
    fun adoptsHigherValueWhenLowerLeavesItUnset() {
        val merged = SpanLimitsConfigModel().mergeWith(SpanLimitsConfigModel(attributeCountLimit = 10))

        assertEquals(10, merged.attributeCountLimit)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredIt() {
        assertEquals(SpanLimitsConfigModel(), SpanLimitsConfigModel().mergeWith(SpanLimitsConfigModel()))
    }

    @Test
    fun treatsZeroAsAConfiguredValue() {
        val merged = SpanLimitsConfigModel(linkCountLimit = 128).mergeWith(SpanLimitsConfigModel(linkCountLimit = 0))

        assertEquals(0, merged.linkCountLimit)
    }

    @Test
    fun refinesRatherThanReplaces() {
        val lower = SpanLimitsConfigModel(
            attributeCountLimit = 1,
            attributeValueLengthLimit = 2,
            linkCountLimit = 3,
            eventCountLimit = 4,
            attributeCountPerEventLimit = 5,
            attributeCountPerLinkLimit = 6,
        )

        val merged = lower.mergeWith(SpanLimitsConfigModel(linkCountLimit = 99))

        assertEquals(99, merged.linkCountLimit)
        assertEquals(1, merged.attributeCountLimit)
        assertEquals(2, merged.attributeValueLengthLimit)
        assertEquals(4, merged.eventCountLimit)
        assertEquals(5, merged.attributeCountPerEventLimit)
        assertEquals(6, merged.attributeCountPerLinkLimit)
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = SpanLimitsConfigModel(
            attributeCountLimit = 1,
            attributeValueLengthLimit = 2,
            linkCountLimit = 3,
            eventCountLimit = 4,
            attributeCountPerEventLimit = 5,
            attributeCountPerLinkLimit = 6,
        )
        val higher = SpanLimitsConfigModel(
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
        val lower = SpanLimitsConfigModel(attributeCountLimit = 1)
        val higher = SpanLimitsConfigModel(linkCountLimit = 2)

        lower.mergeWith(higher)

        assertEquals(SpanLimitsConfigModel(attributeCountLimit = 1), lower)
        assertEquals(SpanLimitsConfigModel(linkCountLimit = 2), higher)
    }
}
