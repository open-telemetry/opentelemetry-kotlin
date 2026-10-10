package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.NameStringValuePair
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpHttpSpanExporterMapperTest {
    @Test
    fun mapsHttp() {
        val exporter = OtlpHttpExporter(
            endpoint = "http://localhost:4317",
            timeout = 10_000,
            headersList = "key=value"
        )
        assertEquals(
            OtlpHttpSpanExporterBehavior(
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
            OtlpHttpSpanExporterBehavior(
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
            OtlpHttpSpanExporterBehavior(
                headers = mapOf("key" to "value", "key3" to "value3")
            ),
            exporter.toBehavior(),
        )
    }
}
