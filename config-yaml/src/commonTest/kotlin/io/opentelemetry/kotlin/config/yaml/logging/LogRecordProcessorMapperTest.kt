package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
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
                    scheduleDelay = BatchLogRecordProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                    exportTimeout = BatchLogRecordProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                    maxQueueSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                    maxExportBatchSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE,
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
                    scheduleDelay = BatchLogRecordProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                    exportTimeout = BatchLogRecordProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                    maxQueueSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                    maxExportBatchSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE,
                    exporter = LogRecordExporterBehavior(
                        console = ConsoleExporterBehavior()
                    )
                )
            ),
            processors.toBehavior()
        )
    }
}
