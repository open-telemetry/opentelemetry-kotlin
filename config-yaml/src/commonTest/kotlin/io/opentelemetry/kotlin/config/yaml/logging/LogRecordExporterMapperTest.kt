package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordExporterMapperTest {
    @Test
    fun mapsExporter() {
        val exporter = LogRecordExporter(
            console = ConsoleExporter(),
            otlpHttp = OtlpHttpExporter(
                endpoint = "http://localhost:3000/logs",
                timeout = 10_000,
                headersList = "key=value"
            )
        )
        assertEquals(
            LogRecordExporterBehavior(
                console = ConsoleExporterBehavior(),
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://localhost:3000/logs",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                )
            ),
            exporter.toBehavior(),
        )
        assertNull(LogRecordExporter().toBehavior())
    }

    @Test
    fun unsetIfNoMappedFields() {
        assertNull(LogRecordExporter().toBehavior())
    }
}
