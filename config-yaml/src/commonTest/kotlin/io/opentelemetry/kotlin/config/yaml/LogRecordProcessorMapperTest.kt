package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.LogExporterBehavior
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
    fun emptyProcessorsLeaveExporterUnset() {
        assertNull(emptyList<LogRecordProcessor>().toExporterBehavior())
    }

    @Test
    fun mapsConsoleFromASimpleProcessor() {
        val processors = listOf(
            LogRecordProcessor(simple = SimpleLogRecordProcessor(exporter = consoleExporter())),
        )
        assertEquals(LogExporterBehavior.Console, processors.toExporterBehavior())
    }

    @Test
    fun mapsConsoleFromABatchProcessor() {
        val processors = listOf(
            LogRecordProcessor(batch = BatchLogRecordProcessor(exporter = consoleExporter())),
        )
        assertEquals(LogExporterBehavior.Console, processors.toExporterBehavior())
    }

    @Test
    fun leavesProcessorsWithoutConsoleUnset() {
        val processors = listOf(
            LogRecordProcessor(simple = SimpleLogRecordProcessor(exporter = LogRecordExporter())),
        )
        assertNull(processors.toExporterBehavior())
    }

    private fun consoleExporter() = LogRecordExporter(console = ConsoleExporter())
}
