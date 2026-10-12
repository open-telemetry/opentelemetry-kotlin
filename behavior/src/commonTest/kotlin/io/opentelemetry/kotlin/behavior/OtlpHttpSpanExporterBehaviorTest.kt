package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OtlpHttpSpanExporterBehaviorTest {
    @Test
    fun everyFieldStartsUnset() {
        val httpExporter = OtlpHttpSpanExporterBehavior()
        assertNull(httpExporter.endpoint)
        assertNull(httpExporter.timeout)
        assertNull(httpExporter.headers)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = OtlpHttpSpanExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
            headers = mapOf("a" to "b"),
        )
        assertEquals(higher, OtlpHttpSpanExporterBehavior().mergeWith(higher))
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = OtlpHttpSpanExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
            headers = mapOf("a" to "b"),
        )
        val higher = OtlpHttpSpanExporterBehavior(
            endpoint = "https://example.com/2",
            timeout = 20_000,
            headers = mapOf("a" to "c", "c" to "d"),
        )
        assertEquals(higher, lower.mergeWith(higher))
    }
}
