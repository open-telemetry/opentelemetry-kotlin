package io.opentelemetry.kotlin.config.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OpenTelemetryConfigModelTest {

    @Test
    fun everyFieldStartsUnset() {
        assertNull(OpenTelemetryConfigModel().tracerProvider)
    }

    @Test
    fun mergingEmptyModelChangesNothing() {
        val populated = OpenTelemetryConfigModel(
            tracerProvider = TracerProviderConfigModel(spanLimits = SpanLimitsConfigModel(linkCountLimit = 3)),
        )

        assertEquals(populated, populated.mergeWith(OpenTelemetryConfigModel()))
    }

    @Test
    fun mergingIntoEmptyModelAdoptsEverything() {
        val populated = OpenTelemetryConfigModel(
            tracerProvider = TracerProviderConfigModel(spanLimits = SpanLimitsConfigModel(linkCountLimit = 3)),
        )

        assertEquals(populated, OpenTelemetryConfigModel().mergeWith(populated))
    }

    @Test
    fun staysUnsetWhenNoLayerConfiguresAnything() {
        assertEquals(
            OpenTelemetryConfigModel(),
            OpenTelemetryConfigModel().mergeWith(OpenTelemetryConfigModel()),
        )
    }

    @Test
    fun mergeRecursesIntoNestedBlocks() {
        val merged = OpenTelemetryConfigModel(
            tracerProvider = TracerProviderConfigModel(
                spanLimits = SpanLimitsConfigModel(attributeCountLimit = 1, eventCountLimit = 4),
            ),
        ).mergeWith(
            OpenTelemetryConfigModel(
                tracerProvider = TracerProviderConfigModel(spanLimits = SpanLimitsConfigModel(eventCountLimit = 99)),
            ),
        )

        assertEquals(1, merged.tracerProvider?.spanLimits?.attributeCountLimit)
        assertEquals(99, merged.tracerProvider?.spanLimits?.eventCountLimit)
    }

    @Test
    fun foldAppliesLayersInPrecedenceOrder() {
        val envLayer = configWithSpanLimits(
            SpanLimitsConfigModel(attributeCountLimit = 1, attributeValueLengthLimit = 2, linkCountLimit = 3),
        )
        val fileLayer = configWithSpanLimits(
            SpanLimitsConfigModel(attributeCountLimit = 10, linkCountLimit = 30),
        )
        val dslLayer = configWithSpanLimits(SpanLimitsConfigModel(attributeCountLimit = 100))

        val merged = mergeConfigModels(listOf(envLayer, fileLayer, dslLayer))

        val limits = merged.tracerProvider?.spanLimits
        assertEquals(100, limits?.attributeCountLimit)
        assertEquals(30, limits?.linkCountLimit)
        assertEquals(2, limits?.attributeValueLengthLimit)
    }

    @Test
    fun foldOfNoLayersIsEmpty() {
        assertEquals(OpenTelemetryConfigModel(), mergeConfigModels(emptyList()))
    }

    @Test
    fun foldOfSingleLayerReturnsThatLayer() {
        val layer = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 3))

        assertEquals(layer, mergeConfigModels(listOf(layer)))
    }

    @Test
    fun foldIgnoresLayersThatConfiguredNothing() {
        val layer = configWithSpanLimits(SpanLimitsConfigModel(linkCountLimit = 3))
        val layers = listOf(OpenTelemetryConfigModel(), layer, OpenTelemetryConfigModel())

        assertEquals(layer, mergeConfigModels(layers))
    }

    private fun configWithSpanLimits(spanLimits: SpanLimitsConfigModel) =
        OpenTelemetryConfigModel(tracerProvider = TracerProviderConfigModel(spanLimits = spanLimits))
}
