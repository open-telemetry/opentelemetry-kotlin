package io.opentelemetry.kotlin.config.yaml

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchLogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.NameStringValuePair
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
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
            LogRecordProcessorBehavior(
                console = ConsoleExporterBehavior(),
                simple = SimpleLogRecordProcessorBehavior(),
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsHttpFromASimpleProcessor() {
        val processors = listOf(
            LogRecordProcessor(simple = SimpleLogRecordProcessor(exporter = httpExporter())),
        )
        assertEquals(
            LogRecordProcessorBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                ),
                simple = SimpleLogRecordProcessorBehavior(),
            ),
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
    fun mapsHttpFromABatchProcessor() {
        val processors = listOf(LogRecordProcessor(batch = BatchLogRecordProcessor(exporter = httpExporter())))
        assertEquals(
            LogRecordProcessorBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                )
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun httpExporterHeaderHaveHigherPriorityThanHeaderList() {
        val processors = listOf(
            LogRecordProcessor(
                batch = BatchLogRecordProcessor(
                    exporter = LogRecordExporter(
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
            LogRecordProcessorBehavior(
                http = OtlpHttpLogsExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                )
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun leavesSimpleUnsetForABatchProcessor() {
        val processors = listOf(
            LogRecordProcessor(batch = BatchLogRecordProcessor(exporter = consoleExporter())),
        )
        assertNull(processors.toBehavior()?.simple)
    }

    @Test
    fun leavesProcessorsWithNoKnownExportersUnset() {
        val processors = listOf(
            LogRecordProcessor(simple = SimpleLogRecordProcessor(exporter = LogRecordExporter())),
        )
        assertNull(processors.toBehavior())
    }

    private fun consoleExporter() = LogRecordExporter(console = ConsoleExporter())
    private fun httpExporter() = LogRecordExporter(
        otlpHttp = OtlpHttpExporter(
            endpoint = "http://localhost:4317",
            timeout = 10_000,
            headersList = "key=value"
        )
    )
}
