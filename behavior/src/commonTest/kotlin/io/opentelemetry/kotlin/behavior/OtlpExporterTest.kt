package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpExporterTest {
    @Test
    fun testBuildHeaderMap() {
        assertEquals(null, OtlpExporter.buildHeaderMap(null))
        assertEquals(OtlpExporter.buildHeaderMap("="), emptyMap())
        assertEquals(
            OtlpExporter.buildHeaderMap("\tkey =    value\t\t"),
            mapOf("key" to "value")
        )
        assertEquals(
            OtlpExporter.buildHeaderMap("key=value,key2=,key3=value3"),
            mapOf("key" to "value", "key3" to "value3")
        )
        assertEquals(
            OtlpExporter.buildHeaderMap("key=value,=value2,key3=value3"),
            mapOf("key" to "value", "key3" to "value3")
        )
        assertEquals(
            OtlpExporter.buildHeaderMap("key=value,key2=value2=value2"),
            mapOf("key" to "value", "key2" to "value2=value2")
        )
        assertEquals(
            OtlpExporter.buildHeaderMap("key=value,key2=value2"),
            mapOf("key" to "value", "key2" to "value2")
        )
        assertEquals(
            OtlpExporter.buildHeaderMap("key=value,garbage,key3=value3"),
            mapOf("key" to "value", "key3" to "value3")
        )
        assertEquals(OtlpExporter.buildHeaderMap("garbage"), emptyMap())
        assertEquals(OtlpExporter.buildHeaderMap("key=value,"), mapOf("key" to "value"))
    }
}
