package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.SimpleLogRecordProcessorBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchLogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.SimpleLogRecordProcessor
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogRecordProcessorMapperTest {

    @Test
    fun emptyProcessorsLeaveBehaviorUnset() {
        assertEquals(LogRecordProcessorBehavior(), emptyList<LogRecordProcessor>().toBehavior())
    }

    @Test
    fun mapsSimpleLogRecordProcessor() {
        val processors = listOf(
            LogRecordProcessor(
                simple = SimpleLogRecordProcessor(exporter = LogRecordExporter(console = ConsoleExporter())),
            )
        )
        assertEquals(
            LogRecordProcessorBehavior(
                simple = SimpleLogRecordProcessorBehavior(
                    exporter = LogRecordExporterBehavior(
                        console = ConsoleExporterBehavior()
                    )
                )
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun mapsBatchLogRecordProcessor() {
        val processors = listOf(
            LogRecordProcessor(
                batch = BatchLogRecordProcessor(exporter = LogRecordExporter(console = ConsoleExporter()))
            )
        )
        assertEquals(
            LogRecordProcessorBehavior(
                batch = BatchLogRecordProcessorBehavior(
                    exporter = LogRecordExporterBehavior(
                        console = ConsoleExporterBehavior()
                    )
                )
            ),
            processors.toBehavior(),
        )
    }

    @Test
    fun leavesSimpleProcessorUnsetForABatchProcessor() {
        val processors = listOf(
            LogRecordProcessor(
                batch = BatchLogRecordProcessor(exporter = LogRecordExporter(console = ConsoleExporter())),
                simple = SimpleLogRecordProcessor(exporter = LogRecordExporter(console = ConsoleExporter()))
            ),
        )
        assertEquals(
            LogRecordProcessorBehavior(
                batch = BatchLogRecordProcessorBehavior(
                    exporter = LogRecordExporterBehavior(
                        console = ConsoleExporterBehavior()
                    )
                )
            ),
            processors.toBehavior()
        )
    }
}
