package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
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
                simple = SimpleSpanProcessorBehavior(
                    exporter = SpanExporterBehavior(console = ConsoleExporterBehavior())
                ),
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
            SpanProcessorBehavior(
                simple = SimpleSpanProcessorBehavior(
                    exporter = SpanExporterBehavior(
                        http = http
                    )
                ),
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
            SpanProcessorBehavior(
                batch = BatchSpanProcessorBehavior(
                    scheduleDelay = BatchSpanProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                    exportTimeout = BatchSpanProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                    maxQueueSize = BatchSpanProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                    maxExportBatchSize = BatchSpanProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE,
                    exporter = SpanExporterBehavior(console = ConsoleExporterBehavior()),
                ),
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsHttpFromABatchProcessor() {
        val processors = listOf(SpanProcessor(batch = BatchSpanProcessor(exporter = httpExporter())))
        assertEquals(
            SpanProcessorBehavior(
                batch = BatchSpanProcessorBehavior(
                    scheduleDelay = BatchSpanProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                    exportTimeout = BatchSpanProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                    maxQueueSize = BatchSpanProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                    maxExportBatchSize = BatchSpanProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE,
                    exporter = SpanExporterBehavior(
                        http = OtlpHttpSpanExporterBehavior(
                            endpoint = "http://localhost:4317",
                            timeout = 10_000,
                            headers = mapOf("key" to "value")
                        )
                    ),
                ),
            ),
            processors.toBehavior(),
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
        assertNull(processors.toBehavior()?.simple)
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
