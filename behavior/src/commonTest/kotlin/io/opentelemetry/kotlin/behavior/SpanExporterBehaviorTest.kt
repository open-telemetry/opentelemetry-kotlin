package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanExporterBehaviorTest {
    @Test
    fun everyFieldStartsUnset() {
        val exporter = SpanExporterBehavior()
        assertNull(exporter.console)
        assertNull(exporter.http)
    }

    @Test
    fun mergesExportersFieldByFieldAcrossLayers() {
        val console = ConsoleExporterBehavior()
        val http = OtlpHttpSpanExporterBehavior(endpoint = "https://example.com")
        val merged = SpanExporterBehavior(console = console).mergeWith(SpanExporterBehavior(http = http))
        assertEquals(SpanExporterBehavior(console = console, http = http), merged)
    }

    @Test
    fun higherLayerWinsPerExporter() {
        val low = OtlpHttpSpanExporterBehavior(endpoint = "https://low.example.com")
        val high = OtlpHttpSpanExporterBehavior(endpoint = "https://high.example.com")
        val merged = SpanExporterBehavior(http = low).mergeWith(SpanExporterBehavior(http = high))
        assertEquals("https://high.example.com", merged.http?.endpoint)
    }
}
