package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.NameStringValuePair
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import io.opentelemetry.kotlin.config.schema.model.SimpleSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.SpanExporter
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanProcessorMapperTest {

    @Test
    fun emptyProcessorsLeaveBehaviorUnset() {
        assertNull(emptyList<SpanProcessor>().toBehavior())
    }

    @Test
    fun mapsConsoleFromASimpleProcessor() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(
            SpanProcessorBehavior(
                console = ConsoleExporterBehavior(),
                simple = SimpleSpanProcessorBehavior(),
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsHttpFromASimpleProcessor() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = httpExporter())),
        )
        assertEquals(
            SpanProcessorBehavior(
                http = OtlpHttpExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                ),
                simple = SimpleSpanProcessorBehavior(),
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
            SpanProcessorBehavior(console = ConsoleExporterBehavior(), batch = BatchSpanProcessorBehavior()),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsHttpFromABatchProcessor() {
        val processors = listOf(SpanProcessor(batch = BatchSpanProcessor(exporter = httpExporter())))
        assertEquals(
            SpanProcessorBehavior(
                http = OtlpHttpExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                ),
                batch = BatchSpanProcessorBehavior(),
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun httpExporterHeaderHaveHigherPriorityThanHeaderList() {
        val processors = listOf(
            SpanProcessor(
                batch = BatchSpanProcessor(
                    exporter = SpanExporter(
                        otlpHttp = OtlpHttpExporter(
                            endpoint = "http://localhost:4317",
                            timeout = 10_000,
                            headersList = "key=value2",
                            headers = listOf(NameStringValuePair("key", "value"))
                        )
                    )
                )
            )
        )
        assertEquals(
            SpanProcessorBehavior(
                http = OtlpHttpExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                ),
                batch = BatchSpanProcessorBehavior(),
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsBatchOptions() {
        val processors = listOf(
            SpanProcessor(
                batch = BatchSpanProcessor(
                    exporter = consoleExporter(),
                    scheduleDelay = 100,
                    exportTimeout = 200,
                    maxQueueSize = 40,
                    maxExportBatchSize = 20,
                )
            )
        )
        assertEquals(
            BatchSpanProcessorBehavior(100, 200, 40, 20),
            processors.toBehavior()?.batch,
        )
    }

    @Test
    fun leavesSimpleUnsetForABatchProcessor() {
        val processors = listOf(
            SpanProcessor(batch = BatchSpanProcessor(exporter = consoleExporter())),
        )
        assertNull(processors.toBehavior()?.simple)
    }

    @Test
    fun leavesProcessorsWithNoKnownExportersUnset() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = SpanExporter())),
        )
        assertNull(processors.toBehavior())
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
