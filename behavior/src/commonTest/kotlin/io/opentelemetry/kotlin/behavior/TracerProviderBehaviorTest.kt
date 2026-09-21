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

    @Test
    fun samplerStartsUnset() {
        assertNull(TracerProviderBehavior().sampler)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredSampler() {
        assertNull(TracerProviderBehavior().mergeWith(TracerProviderBehavior()).sampler)
    }

    @Test
    fun adoptsSamplerFromWhicheverLayerSuppliedIt() {
        val sampler = SamplerBehavior.AlwaysOff
        assertEquals(
            sampler,
            TracerProviderBehavior().mergeWith(TracerProviderBehavior(sampler = sampler)).sampler,
        )
        assertEquals(
            sampler,
            TracerProviderBehavior(sampler = sampler).mergeWith(TracerProviderBehavior()).sampler,
        )
    }

    @Test
    fun samplerMergeDoesNotDropSpanLimits() {
        val spanLimits = SpanLimitsBehavior(linkCountLimit = 3)
        val merged = TracerProviderBehavior(spanLimits = spanLimits).mergeWith(
            TracerProviderBehavior(sampler = SamplerBehavior.AlwaysOff)
        )

        assertEquals(spanLimits, merged.spanLimits)
        assertEquals(SamplerBehavior.AlwaysOff, merged.sampler)
    }

    @Test
    fun idGeneratorStartsUnset() {
        assertNull(TracerProviderBehavior().idGenerator)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredIdGenerator() {
        assertNull(TracerProviderBehavior().mergeWith(TracerProviderBehavior()).idGenerator)
    }

    @Test
    fun adoptsIdGeneratorFromWhicheverLayerSuppliedIt() {
        val idGenerator = IdGeneratorBehavior.Random

        assertEquals(
            idGenerator,
            TracerProviderBehavior().mergeWith(TracerProviderBehavior(idGenerator = idGenerator)).idGenerator,
        )
        assertEquals(
            idGenerator,
            TracerProviderBehavior(idGenerator = idGenerator).mergeWith(TracerProviderBehavior()).idGenerator,
        )
    }

    @Test
    fun idGeneratorMergeDoesNotDropOtherConfiguration() {
        val spanLimits = SpanLimitsBehavior(linkCountLimit = 3)
        val merged = TracerProviderBehavior(spanLimits = spanLimits).mergeWith(
            TracerProviderBehavior(idGenerator = IdGeneratorBehavior.Random),
        )

        assertEquals(spanLimits, merged.spanLimits)
        assertEquals(IdGeneratorBehavior.Random, merged.idGenerator)
    }
}
