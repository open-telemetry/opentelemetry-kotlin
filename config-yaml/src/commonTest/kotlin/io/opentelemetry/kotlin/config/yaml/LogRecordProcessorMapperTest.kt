package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchLogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.SimpleLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorMapperTest {

    @Test
    fun emptyProcessorsLeaveBehaviorUnset() {
        assertNull(emptyList<LogRecordProcessor>().toBehavior())
    }

    @Test
    fun mapsConsoleFromASimpleProcessor() {
        val processors = listOf(
            LogRecordProcessor(simple = SimpleLogRecordProcessor(exporter = consoleExporter())),
        )
        assertEquals(
            LogRecordProcessorBehavior(console = ConsoleExporterBehavior()),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsConsoleFromABatchProcessor() {
        val processors = listOf(
            LogRecordProcessor(batch = BatchLogRecordProcessor(exporter = consoleExporter())),
        )
        assertEquals(
            LogRecordProcessorBehavior(console = ConsoleExporterBehavior()),
            processors.toBehavior(),
        )
    }

    @Test
    fun leavesProcessorsWithoutConsoleUnset() {
        val processors = listOf(
            LogRecordProcessor(simple = SimpleLogRecordProcessor(exporter = LogRecordExporter())),
        )
        assertNull(processors.toBehavior())
    }

    private fun consoleExporter() = LogRecordExporter(console = ConsoleExporter())
}
