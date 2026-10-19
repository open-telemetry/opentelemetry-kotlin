package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OtlpHttpLogsExporterBehaviorTest {
    @Test
    fun everyFieldStartsUnset() {
        val httpExporter = OtlpHttpLogsExporterBehavior()
        assertNull(httpExporter.endpoint)
        assertNull(httpExporter.timeout)
        assertNull(httpExporter.headers)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = OtlpHttpLogsExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
            headers = mapOf("a" to "b"),
        )
        assertEquals(higher, OtlpHttpLogsExporterBehavior().mergeWith(higher))
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = OtlpHttpLogsExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
            headers = mapOf("a" to "b"),
        )
        val higher = OtlpHttpLogsExporterBehavior(
            endpoint = "https://example.com/2",
            timeout = 20_000,
            headers = mapOf("a" to "c", "c" to "d"),
        )
        assertEquals(higher, lower.mergeWith(higher))
    }
}
