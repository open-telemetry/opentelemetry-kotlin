package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TracerProviderBehaviorTest {

    @Test
    fun spanLimitsStartUnset() {
        assertNull(TracerProviderBehavior().spanLimits)
    }

    @Test
    fun processorStartsUnset() {
        assertNull(TracerProviderBehavior().processor)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredSpanLimits() {
        assertNull(TracerProviderBehavior().mergeWith(TracerProviderBehavior()).spanLimits)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredProcessor() {
        assertNull(TracerProviderBehavior().mergeWith(TracerProviderBehavior()).processor)
    }

    @Test
    fun adoptsSpanLimitsFromWhicheverLayerSuppliedThem() {
        val limits = SpanLimitsBehavior(linkCountLimit = 3)

        assertEquals(
            limits,
            TracerProviderBehavior().mergeWith(TracerProviderBehavior(spanLimits = limits)).spanLimits,
        )
        assertEquals(
            limits,
            TracerProviderBehavior(spanLimits = limits).mergeWith(TracerProviderBehavior()).spanLimits,
        )
    }

    @Test
    fun adoptsProcessorFromWhicheverLayerSuppliedIt() {
        val processor = SpanProcessorBehavior()

        assertEquals(processor, TracerProviderBehavior().mergeWith(TracerProviderBehavior(processor = processor)).processor)
        assertEquals(processor, TracerProviderBehavior(processor = processor).mergeWith(TracerProviderBehavior()).processor)
    }

    @Test
    fun mergesSpanLimitsWhenBothLayersSuppliedThem() {
        val merged = TracerProviderBehavior(
            spanLimits = SpanLimitsBehavior(attributeCountLimit = 1, linkCountLimit = 3),
        ).mergeWith(
            TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 99)),
        )

        assertEquals(1, merged.spanLimits?.attributeCountLimit)
        assertEquals(99, merged.spanLimits?.linkCountLimit)
    }
}
