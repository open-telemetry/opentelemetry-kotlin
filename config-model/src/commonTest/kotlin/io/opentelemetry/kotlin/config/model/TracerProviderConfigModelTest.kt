package io.opentelemetry.kotlin.config.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TracerProviderConfigModelTest {

    @Test
    fun spanLimitsStartUnset() {
        assertNull(TracerProviderConfigModel().spanLimits)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredSpanLimits() {
        assertNull(TracerProviderConfigModel().mergeWith(TracerProviderConfigModel()).spanLimits)
    }

    @Test
    fun adoptsSpanLimitsFromWhicheverLayerSuppliedThem() {
        val limits = SpanLimitsConfigModel(linkCountLimit = 3)

        assertEquals(
            limits,
            TracerProviderConfigModel().mergeWith(TracerProviderConfigModel(spanLimits = limits)).spanLimits,
        )
        assertEquals(
            limits,
            TracerProviderConfigModel(spanLimits = limits).mergeWith(TracerProviderConfigModel()).spanLimits,
        )
    }

    @Test
    fun mergesSpanLimitsWhenBothLayersSuppliedThem() {
        val merged = TracerProviderConfigModel(
            spanLimits = SpanLimitsConfigModel(attributeCountLimit = 1, linkCountLimit = 3),
        ).mergeWith(
            TracerProviderConfigModel(spanLimits = SpanLimitsConfigModel(linkCountLimit = 99)),
        )

        assertEquals(1, merged.spanLimits?.attributeCountLimit)
        assertEquals(99, merged.spanLimits?.linkCountLimit)
    }
}
