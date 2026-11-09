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
            BatchLogRecordProcessorBehavior(),
            behavior
        )
    }

    @Test
    fun mapsFields() {
        val behavior = BatchLogRecordProcessor(
            scheduleDelay = 100,
            exportTimeout = 200,
            maxQueueSize = 400,
            maxExportBatchSize = 300,
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
                maxQueueSize = 400,
                maxExportBatchSize = 300,
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
            BatchLogRecordProcessorBehavior(),
            behavior
        )
    }

    @Test
    fun batchSizeIsCappedAtQueueSize() {
        val behavior = BatchLogRecordProcessor(
            maxQueueSize = 100,
            maxExportBatchSize = 400,
            exporter = LogRecordExporter()
        ).toBehavior()
        assertEquals(100, behavior.maxQueueSize)
        assertEquals(100, behavior.maxExportBatchSize)
    }

    @Test
    fun defaultBatchSizeIsCappedAtQueueSize() {
        val behavior = BatchLogRecordProcessor(maxQueueSize = 100, exporter = LogRecordExporter()).toBehavior()
        assertEquals(100, behavior.maxExportBatchSize)
    }
}
