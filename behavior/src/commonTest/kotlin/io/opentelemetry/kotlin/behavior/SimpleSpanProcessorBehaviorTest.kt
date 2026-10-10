package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SimpleSpanProcessorBehaviorTest {
    @Test
    fun everyFieldStartsUnset() {
        val processor = SimpleSpanProcessorBehavior()
        assertEquals(null, processor.exporter)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = SimpleSpanProcessorBehavior(exporter = SpanExporterBehavior())
        assertEquals(higher, SimpleSpanProcessorBehavior().mergeWith(higher))
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = SimpleSpanProcessorBehavior(
            exporter = SpanExporterBehavior(http = OtlpHttpSpanExporterBehavior(endpoint = "www.example1.com"))
        )
        val higher = SimpleSpanProcessorBehavior(
            exporter = SpanExporterBehavior(http = OtlpHttpSpanExporterBehavior(endpoint = "www.example2.com"))
        )
        assertEquals(higher, lower.mergeWith(higher))
    }
}
