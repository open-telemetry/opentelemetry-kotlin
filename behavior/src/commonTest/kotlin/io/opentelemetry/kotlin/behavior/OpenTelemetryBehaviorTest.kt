package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OpenTelemetryBehaviorTest {

    @Test
    fun everyFieldStartsUnset() {
        val behavior = OpenTelemetryBehavior()

        assertNull(behavior.tracerProvider)
        assertNull(behavior.loggerProvider)
    }

    @Test
    fun mergingEmptyBehaviorChangesNothing() {
        val populated = OpenTelemetryBehavior(
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )

        assertEquals(populated, populated.mergeWith(OpenTelemetryBehavior()))
    }

    @Test
    fun mergingIntoEmptyBehaviorAdoptsEverything() {
        val populated = OpenTelemetryBehavior(
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )

        assertEquals(populated, OpenTelemetryBehavior().mergeWith(populated))
    }

    @Test
    fun staysUnsetWhenNoLayerConfiguresAnything() {
        assertEquals(
            OpenTelemetryBehavior(),
            OpenTelemetryBehavior().mergeWith(OpenTelemetryBehavior()),
        )
    }

    @Test
    fun mergeRecursesIntoNestedBlocks() {
        val merged = OpenTelemetryBehavior(
            tracerProvider = TracerProviderBehavior(
                spanLimits = SpanLimitsBehavior(attributeCountLimit = 1, eventCountLimit = 4),
            ),
        ).mergeWith(
            OpenTelemetryBehavior(
                tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(eventCountLimit = 99)),
            ),
        )

        assertEquals(1, merged.tracerProvider?.spanLimits?.attributeCountLimit)
        assertEquals(99, merged.tracerProvider?.spanLimits?.eventCountLimit)
    }

    @Test
    fun mergesTracingAndLoggingBranchesIndependently() {
        val tracing = OpenTelemetryBehavior(
            tracerProvider = TracerProviderBehavior(spanLimits = SpanLimitsBehavior(linkCountLimit = 3)),
        )
        val logging = OpenTelemetryBehavior(
            loggerProvider = LoggerProviderBehavior(logLimits = LogLimitsBehavior(attributeCountLimit = 7)),
        )

        val merged = tracing.mergeWith(logging)

        assertEquals(3, merged.tracerProvider?.spanLimits?.linkCountLimit)
        assertEquals(7, merged.loggerProvider?.logLimits?.attributeCountLimit)
    }

    @Test
    fun foldAppliesLayersInPrecedenceOrder() {
        val envLayer = configWithSpanLimits(
            SpanLimitsBehavior(attributeCountLimit = 1, attributeValueLengthLimit = 2, linkCountLimit = 3),
        )
        val fileLayer = configWithSpanLimits(
            SpanLimitsBehavior(attributeCountLimit = 10, linkCountLimit = 30),
        )
        val dslLayer = configWithSpanLimits(SpanLimitsBehavior(attributeCountLimit = 100))

        val merged = mergeBehaviors(listOf(envLayer, fileLayer, dslLayer))

        val limits = merged.tracerProvider?.spanLimits
        assertEquals(100, limits?.attributeCountLimit)
        assertEquals(30, limits?.linkCountLimit)
        assertEquals(2, limits?.attributeValueLengthLimit)
    }

    @Test
    fun foldOfNoLayersIsEmpty() {
        assertEquals(OpenTelemetryBehavior(), mergeBehaviors(emptyList()))
    }

    @Test
    fun foldOfSingleLayerReturnsThatLayer() {
        val layer = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 3))

        assertEquals(layer, mergeBehaviors(listOf(layer)))
    }

    @Test
    fun foldIgnoresLayersThatConfiguredNothing() {
        val layer = configWithSpanLimits(SpanLimitsBehavior(linkCountLimit = 3))
        val layers = listOf(OpenTelemetryBehavior(), layer, OpenTelemetryBehavior())

        assertEquals(layer, mergeBehaviors(layers))
    }

    private fun configWithSpanLimits(spanLimits: SpanLimitsBehavior) =
        OpenTelemetryBehavior(tracerProvider = TracerProviderBehavior(spanLimits = spanLimits))
}
