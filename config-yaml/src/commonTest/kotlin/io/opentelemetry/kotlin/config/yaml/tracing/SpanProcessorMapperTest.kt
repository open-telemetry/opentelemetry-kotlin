package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.tracing.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.tracing.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.tracing.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import io.opentelemetry.kotlin.config.schema.model.SimpleSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.SpanExporter
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanProcessorMapperTest {

    @Test
    fun emptyProcessorsLeaveBehaviorUnset() {
        assertEquals(
            emptyList(),
            emptyList<SpanProcessor>().toBehavior()
        )
    }

    @Test
    fun mapsMultipleProcessors() {
        val http = OtlpHttpSpanExporterBehavior(
            endpoint = "http://localhost:4317",
            timeout = 10_000,
            headers = mapOf("key" to "value")
        )
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = consoleExporter())),
            SpanProcessor(batch = BatchSpanProcessor(exporter = httpExporter())),
        )
        assertEquals(
            listOf(
                SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(console = ConsoleExporterBehavior())
                    )
                ),
                SpanProcessorBehavior(
                    batch = BatchSpanProcessorBehavior(exporter = SpanExporterBehavior(http = http))
                )
            ),
            processors.toBehavior()
        )
    }

    @Test
    fun mapsConsoleFromASimpleProcessor() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(
            listOf(
                SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(console = ConsoleExporterBehavior())
                    ),
                )
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsHttpFromASimpleProcessor() {
        val http = OtlpHttpSpanExporterBehavior(
            endpoint = "http://localhost:4317",
            timeout = 10_000,
            headers = mapOf("key" to "value")
        )
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = httpExporter())),
        )
        assertEquals(
            listOf(
                SpanProcessorBehavior(
                    simple = SimpleSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            http = http
                        )
                    ),
                )
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsConsoleFromABatchProcessor() {
        val processors = listOf(
            SpanProcessor(batch = BatchSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(
            listOf(
                SpanProcessorBehavior(
                    batch = BatchSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(console = ConsoleExporterBehavior()),
                    ),
                )
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsHttpFromABatchProcessor() {
        val processors = listOf(SpanProcessor(batch = BatchSpanProcessor(exporter = httpExporter())))
        assertEquals(
            listOf(
                SpanProcessorBehavior(
                    batch = BatchSpanProcessorBehavior(
                        exporter = SpanExporterBehavior(
                            http = OtlpHttpSpanExporterBehavior(
                                endpoint = "http://localhost:4317",
                                timeout = 10_000,
                                headers = mapOf("key" to "value")
                            )
                        ),
                    ),
                )
            ),
            processors.toBehavior(),
        )
    }

    private fun consoleExporter() = SpanExporter(console = ConsoleExporter())
    private fun httpExporter() = SpanExporter(
        otlpHttp = OtlpHttpExporter(
            endpoint = "http://localhost:4317",
            timeout = 10_000,
            headersList = "key=value"
        )
    )
}
