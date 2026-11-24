package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import io.opentelemetry.kotlin.config.schema.model.SimpleLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SimpleLogRecordProcessorMapperTest {
    @Test
    fun mapsSimpleLogRecordProcessor() {
        val processor = SimpleLogRecordProcessor(
            exporter = LogRecordExporter(
                console = ConsoleExporter(),
                otlpHttp = OtlpHttpExporter(
                    endpoint = "http://localhost:3000/logs",
                    timeout = 10_000,
                    headersList = "key=value"
                )
            )
        )
        assertEquals(
            SimpleLogRecordProcessorBehavior(
                exporter = LogRecordExporterBehavior(
                    console = ConsoleExporterBehavior(),
                    http = OtlpHttpLogsExporterBehavior(
                        endpoint = "http://localhost:3000/logs",
                        timeout = 10_000,
                        headers = mapOf("key" to "value")
                    )
                )
            ),
            processor.toBehavior(),
        )
    }

    @Test
    fun unsetIfNoMappedFields() {
        assertEquals(
            SimpleLogRecordProcessorBehavior(exporter = LogRecordExporterBehavior()),
            SimpleLogRecordProcessor(exporter = LogRecordExporter()).toBehavior()
        )
    }
}
