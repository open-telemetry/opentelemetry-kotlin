package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpExporterTest {
    @Test
    fun testBuildHeaderMap() {
        assertEquals(null, OtlpExporter.buildHeaderMap(null))
        assertEquals(emptyMap(), OtlpExporter.buildHeaderMap("="))
        assertEquals(
            mapOf("key" to "value"),
            OtlpExporter.buildHeaderMap("\tkey =    value\t\t")
        )
        assertEquals(
            mapOf("key" to "value", "key3" to "value3"),
            OtlpExporter.buildHeaderMap("key=value,key2=,key3=value3")
        )
        assertEquals(
            mapOf("key" to "value", "key3" to "value3"),
            OtlpExporter.buildHeaderMap("key=value,=value2,key3=value3")
        )
        assertEquals(
            mapOf("key" to "value", "key2" to "value2=value2"),
            OtlpExporter.buildHeaderMap("key=value,key2=value2=value2")
        )
        assertEquals(
            mapOf("key" to "value", "key2" to "value2"),
            OtlpExporter.buildHeaderMap("key=value,key2=value2")
        )
        assertEquals(
            mapOf("key" to "value", "key3" to "value3"),
            OtlpExporter.buildHeaderMap("key=value,garbage,key3=value3")
        )
        assertEquals(emptyMap(), OtlpExporter.buildHeaderMap("garbage"))
        assertEquals(mapOf("key" to "value"), OtlpExporter.buildHeaderMap("key=value,"))
    }
}
