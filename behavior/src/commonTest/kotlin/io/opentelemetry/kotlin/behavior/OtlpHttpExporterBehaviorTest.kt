package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class OtlpHttpExporterBehaviorTest {
    @Test
    fun everyFieldStartsUnset() {
        val httpExporter = OtlpHttpExporterBehavior()
        assertNull(httpExporter.endpoint)
        assertNull(httpExporter.timeout)
        assertNull(httpExporter.headers)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = OtlpHttpExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
            headers = mapOf("a" to "b"),
        )
        assertEquals(higher, OtlpHttpExporterBehavior().mergeWith(higher))
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = OtlpHttpExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
            headers = mapOf("a" to "b"),
        )
        val higher = OtlpHttpExporterBehavior(
            endpoint = "https://example.com/2",
            timeout = 20_000,
            headers = mapOf("a" to "c", "c" to "d"),
        )
        assertEquals(higher, lower.mergeWith(higher))
    }

    @Test
    fun testBuildHeaderMap() {
        assertEquals(null, OtlpHttpExporterBehavior.buildHeaderMap(null))
        assertEquals(emptyMap(), OtlpHttpExporterBehavior.buildHeaderMap("="))
        assertEquals(
            mapOf("key" to "value"),
            OtlpHttpExporterBehavior.buildHeaderMap("\tkey =    value\t\t")
        )
        assertEquals(
            mapOf("key" to "value", "key3" to "value3"),
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,key2=,key3=value3")
        )
        assertEquals(
            mapOf("key" to "value", "key3" to "value3"),
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,=value2,key3=value3")
        )
        assertEquals(
            mapOf("key" to "value", "key2" to "value2=value2"),
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,key2=value2=value2")
        )
        assertEquals(
            mapOf("key" to "value", "key2" to "value2"),
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,key2=value2")
        )
        assertEquals(
            mapOf("key" to "value", "key3" to "value3"),
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,garbage,key3=value3")
        )
        assertEquals(emptyMap(), OtlpHttpExporterBehavior.buildHeaderMap("garbage"))
        assertEquals(mapOf("key" to "value"), OtlpHttpExporterBehavior.buildHeaderMap("key=value,"))
    }
}
