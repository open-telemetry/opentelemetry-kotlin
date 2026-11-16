package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanProcessorBehaviorTest {
    @Test
    fun simpleStartsUnset() {
        assertNull(SpanProcessorBehavior().simple)
    }

    @Test
    fun simpleStaysUnsetWhenNeitherLayerConfigured() {
        assertNull(SpanProcessorBehavior().mergeWith(SpanProcessorBehavior()).simple)
    }

    @Test
    fun adoptsSimpleFromWhicheverLayerSuppliedIt() {
        val simple = SimpleSpanProcessorBehavior()

        assertEquals(
            simple,
            SpanProcessorBehavior().mergeWith(SpanProcessorBehavior(simple = simple)).simple,
        )
        assertEquals(
            simple,
            SpanProcessorBehavior(simple = simple).mergeWith(SpanProcessorBehavior()).simple,
        )
    }

    @Test
    fun keepsExporterConfigurationWhenSelectingSimple() {
        val merged = SpanProcessorBehavior().mergeWith(
            SpanProcessorBehavior(simple = SimpleSpanProcessorBehavior()),
        )

        assertEquals(SimpleSpanProcessorBehavior(), merged.simple)
    }
}
