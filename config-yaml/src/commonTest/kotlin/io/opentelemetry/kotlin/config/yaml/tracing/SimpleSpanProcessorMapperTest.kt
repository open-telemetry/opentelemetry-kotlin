package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import io.opentelemetry.kotlin.config.schema.model.SimpleSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.SpanExporter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SimpleSpanProcessorMapperTest {
    @Test
    fun mapsSimpleSpanProcessor() {
        val processor = SimpleSpanProcessor(
            exporter = SpanExporter(
                console = ConsoleExporter(),
                otlpHttp = OtlpHttpExporter(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headersList = "key=value"
                )
            )
        )
        assertEquals(
            SimpleSpanProcessorBehavior(
                exporter = SpanExporterBehavior(
                    console = ConsoleExporterBehavior(),
                    http = OtlpHttpSpanExporterBehavior(
                        endpoint = "http://localhost:4317",
                        timeout = 10_000,
                        headers = mapOf("key" to "value")
                    )
                )
            ),
            processor.toBehavior(),
        )
    }

    @Test
    fun missingExporterLeavesBehaviorUnset() {
        val processor = SimpleSpanProcessor(exporter = SpanExporter())
        assertNull(processor.toBehavior())
    }
}
