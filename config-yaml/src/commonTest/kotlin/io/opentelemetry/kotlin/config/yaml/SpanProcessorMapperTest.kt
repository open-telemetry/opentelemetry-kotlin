package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.SimpleSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.SpanExporter
import io.opentelemetry.kotlin.config.schema.model.SpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanProcessorMapperTest {

    @Test
    fun emptyProcessorsLeaveExporterUnset() {
        assertNull(emptyList<SpanProcessor>().toExporterBehavior())
    }

    @Test
    fun mapsConsoleFromASimpleProcessor() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(SpanExporterBehavior.Console, processors.toExporterBehavior())
    }

    @Test
    fun mapsConsoleFromABatchProcessor() {
        val processors = listOf(
            SpanProcessor(batch = BatchSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(SpanExporterBehavior.Console, processors.toExporterBehavior())
    }

    @Test
    fun leavesProcessorsWithoutConsoleUnset() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = SpanExporter())),
        )
        assertNull(processors.toExporterBehavior())
    }

    @Test
    fun usesFirstProcessorWhenMultiplePresent() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = consoleExporter())),
            SpanProcessor(batch = BatchSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(SpanExporterBehavior.Console, processors.toExporterBehavior())
    }

    @Test
    fun unsupportedExporterReturnsNull() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = SpanExporter())),
        )
        assertNull(processors.toExporterBehavior())
    }

    @Test
    fun processorWithoutExporterReturnsNull() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = SpanExporter())),
        )
        assertNull(processors.toExporterBehavior())
    }

    private fun consoleExporter() = SpanExporter(console = ConsoleExporter())
}
