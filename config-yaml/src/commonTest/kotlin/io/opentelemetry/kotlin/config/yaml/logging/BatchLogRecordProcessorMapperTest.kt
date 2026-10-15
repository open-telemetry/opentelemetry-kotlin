package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.LogRecordExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpLogsExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchLogRecordProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.LogRecordExporter
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BatchLogRecordProcessorMapperTest {
    @Test
    fun everyFieldStartsWithDefaultValue() {
        val behavior = BatchLogRecordProcessor(exporter = LogRecordExporter()).toBehavior()
        assertEquals(
            BatchLogRecordProcessorBehavior(
                scheduleDelay = BatchLogRecordProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                exportTimeout = BatchLogRecordProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                maxQueueSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                maxExportBatchSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE
            ),
            behavior
        )
    }

    @Test
    fun mapsFields() {
        val behavior = BatchLogRecordProcessor(
            scheduleDelay = 100,
            exportTimeout = 200,
            maxQueueSize = 300,
            maxExportBatchSize = 400,
            exporter = LogRecordExporter(
                console = ConsoleExporter(),
                otlpHttp = OtlpHttpExporter(
                    endpoint = "http://localhost:4318/logs"
                )
            )
        ).toBehavior()
        assertEquals(
            BatchLogRecordProcessorBehavior(
                scheduleDelay = 100,
                exportTimeout = 200,
                maxQueueSize = 300,
                maxExportBatchSize = 400,
                exporter = LogRecordExporterBehavior(
                    console = ConsoleExporterBehavior(),
                    http = OtlpHttpLogsExporterBehavior(
                        endpoint = "http://localhost:4318/logs"
                    )
                )
            ),
            behavior
        )
    }

    @Test
    fun invalidFieldValuesFallbackToDefault() {
        val behavior = BatchLogRecordProcessor(
            scheduleDelay = -1,
            exportTimeout = -1,
            maxQueueSize = 0,
            maxExportBatchSize = 0,
            exporter = LogRecordExporter()
        ).toBehavior()
        assertEquals(
            BatchLogRecordProcessorBehavior(
                scheduleDelay = BatchLogRecordProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                exportTimeout = BatchLogRecordProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                maxQueueSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                maxExportBatchSize = BatchLogRecordProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE
            ),
            behavior
        )
    }
}
