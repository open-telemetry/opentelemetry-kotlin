package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpHttpExporterBehaviorTest {
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

    @Test
    fun testBuildHeaderMap() {
        assertEquals(null, OtlpHttpExporterBehavior.buildHeaderMap(null))
        assertEquals(OtlpHttpExporterBehavior.buildHeaderMap("="), emptyMap())
        assertEquals(
            OtlpHttpExporterBehavior.buildHeaderMap("\tkey =    value\t\t"),
            mapOf("key" to "value")
        )
        assertEquals(
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,key2=,key3=value3"),
            mapOf("key" to "value", "key3" to "value3")
        )
        assertEquals(
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,=value2,key3=value3"),
            mapOf("key" to "value", "key3" to "value3")
        )
        assertEquals(
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,key2=value2=value2"),
            mapOf("key" to "value", "key2" to "value2=value2")
        )
        assertEquals(
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,key2=value2"),
            mapOf("key" to "value", "key2" to "value2")
        )
        assertEquals(
            OtlpHttpExporterBehavior.buildHeaderMap("key=value,garbage,key3=value3"),
            mapOf("key" to "value", "key3" to "value3")
        )
        assertEquals(OtlpHttpExporterBehavior.buildHeaderMap("garbage"), emptyMap())
        assertEquals(OtlpHttpExporterBehavior.buildHeaderMap("key=value,"), mapOf("key" to "value"))
    }
}
