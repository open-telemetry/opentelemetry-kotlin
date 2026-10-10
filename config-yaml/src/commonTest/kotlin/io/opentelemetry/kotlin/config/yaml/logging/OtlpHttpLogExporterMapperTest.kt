package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.NameStringValuePair
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpHttpLogExporterMapperTest {
    @Test
    fun mapsHttp() {
        val exporter = OtlpHttpExporter(
            endpoint = "http://localhost:4317",
            timeout = 10_000,
            headersList = "key=value"
        )
        assertEquals(
            OtlpHttpLogsExporterBehavior(
                endpoint = "http://localhost:4317",
                timeout = 10_000,
                headers = mapOf("key" to "value")
            ),
            exporter.toBehavior(),
        )
    }

    @Test
    fun httpExporterHeaderHaveHigherPriorityThanHeaderList() {
        val exporter = OtlpHttpExporter(
            endpoint = "http://localhost:4317",
            timeout = 10_000,
            headersList = "key=value2",
            headers = listOf(NameStringValuePair("key", "value"))
        )
        assertEquals(
            OtlpHttpLogsExporterBehavior(
                endpoint = "http://localhost:4317",
                timeout = 10_000,
                headers = mapOf("key" to "value")
            ),
            exporter.toBehavior(),
        )
    }

    @Test
    fun httpExporterSkipsMalformedHeaders() {
        val exporter = OtlpHttpExporter(headersList = "key=value,key2,key3=value3",)
        assertEquals(
            OtlpHttpLogsExporterBehavior(
                headers = mapOf("key" to "value", "key3" to "value3")
            ),
            exporter.toBehavior(),
        )
    }
}
