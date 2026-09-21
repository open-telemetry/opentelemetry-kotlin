package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
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
    fun emptyProcessorsLeaveBehaviorUnset() {
        assertNull(emptyList<SpanProcessor>().toBehavior())
    }

    @Test
    fun mapsConsoleFromASimpleProcessor() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(SpanProcessorBehavior(console = ConsoleExporterBehavior()), processors.toBehavior())
    }

    @Test
    fun mapsConsoleFromABatchProcessor() {
        val processors = listOf(
            SpanProcessor(batch = BatchSpanProcessor(exporter = consoleExporter())),
        )
        assertEquals(SpanProcessorBehavior(console = ConsoleExporterBehavior()), processors.toBehavior())
    }

    @Test
    fun leavesProcessorsWithoutConsoleUnset() {
        val processors = listOf(
            SpanProcessor(simple = SimpleSpanProcessor(exporter = SpanExporter())),
        )
        assertNull(processors.toBehavior())
    }

    private fun consoleExporter() = SpanExporter(console = ConsoleExporter())
}
