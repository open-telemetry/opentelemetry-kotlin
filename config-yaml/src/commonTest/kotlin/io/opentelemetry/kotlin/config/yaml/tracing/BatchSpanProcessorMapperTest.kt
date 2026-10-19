package io.opentelemetry.kotlin.config.yaml.tracing

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.OtlpHttpSpanExporterBehavior
import io.opentelemetry.kotlin.behavior.SpanExporterBehavior
import io.opentelemetry.kotlin.config.schema.model.BatchSpanProcessor
import io.opentelemetry.kotlin.config.schema.model.ConsoleExporter
import io.opentelemetry.kotlin.config.schema.model.NameStringValuePair
import io.opentelemetry.kotlin.config.schema.model.OtlpHttpExporter
import io.opentelemetry.kotlin.config.schema.model.SpanExporter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BatchSpanProcessorMapperTest {

    @Test
    fun everyFieldStartsWithDefaultValue() {
        val behavior = BatchSpanProcessor(exporter = SpanExporter()).toBehavior()
        assertEquals(
            BatchSpanProcessorBehavior(
                scheduleDelay = BatchSpanProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                exportTimeout = BatchSpanProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                maxQueueSize = BatchSpanProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                maxExportBatchSize = BatchSpanProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE
            ),
            behavior
        )
    }

    @Test
    fun mapsFields() {
        val behavior = BatchSpanProcessor(
            scheduleDelay = 100,
            exportTimeout = 200,
            maxQueueSize = 400,
            maxExportBatchSize = 300,
            exporter = SpanExporter()
        ).toBehavior()
        assertEquals(
            BatchSpanProcessorBehavior(
                scheduleDelay = 100,
                exportTimeout = 200,
                maxQueueSize = 400,
                maxExportBatchSize = 300
            ),
            behavior
        )
    }

    @Test
    fun invalidFieldValuesFallbackToDefault() {
        val behavior = BatchSpanProcessor(
            scheduleDelay = -1,
            exportTimeout = -1,
            maxQueueSize = 0,
            maxExportBatchSize = 0,
            exporter = SpanExporter()
        ).toBehavior()
        assertEquals(
            BatchSpanProcessorBehavior(
                scheduleDelay = BatchSpanProcessorBehavior.DEFAULT_SCHEDULE_DELAY,
                exportTimeout = BatchSpanProcessorBehavior.DEFAULT_EXPORT_TIMEOUT,
                maxQueueSize = BatchSpanProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE,
                maxExportBatchSize = BatchSpanProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE
            ),
            behavior
        )
    }

    @Test
    fun outOfRangeSizesFallbackToDefault() {
        val behavior = BatchSpanProcessor(
            maxQueueSize = Int.MAX_VALUE.toLong() + 1,
            maxExportBatchSize = Int.MAX_VALUE.toLong() + 1,
            exporter = SpanExporter()
        ).toBehavior()
        assertEquals(BatchSpanProcessorBehavior.DEFAULT_MAX_QUEUE_SIZE, behavior.maxQueueSize)
        assertEquals(BatchSpanProcessorBehavior.DEFAULT_MAX_EXPORT_BATCH_SIZE, behavior.maxExportBatchSize)
    }

    @Test
    fun batchSizeIsCappedAtQueueSize() {
        val behavior = BatchSpanProcessor(
            maxQueueSize = 100,
            maxExportBatchSize = 400,
            exporter = SpanExporter()
        ).toBehavior()
        assertEquals(100, behavior.maxQueueSize)
        assertEquals(100, behavior.maxExportBatchSize)
    }

    @Test
    fun defaultBatchSizeIsCappedAtQueueSize() {
        val behavior = BatchSpanProcessor(maxQueueSize = 100, exporter = SpanExporter()).toBehavior()
        assertEquals(100, behavior.maxExportBatchSize)
    }

    @Test
    fun mapsExporter() {
        val behavior = BatchSpanProcessor(
            exporter = SpanExporter(
                console = ConsoleExporter(),
                otlpHttp = OtlpHttpExporter(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headersList = "key=value"
                )
            )
        ).toBehavior()
        assertEquals(
            SpanExporterBehavior(
                console = ConsoleExporterBehavior(),
                http = OtlpHttpSpanExporterBehavior(
                    endpoint = "http://localhost:4317",
                    timeout = 10_000,
                    headers = mapOf("key" to "value")
                )
            ),
            behavior.exporter,
        )
    }

    @Test
    fun exporterHeaderHasHigherPriorityThanHeaderList() {
        val behavior = BatchSpanProcessor(
            exporter = SpanExporter(
                otlpHttp = OtlpHttpExporter(
                    headersList = "key=value2",
                    headers = listOf(NameStringValuePair("key", "value"))
                )
            )
        ).toBehavior()
        assertEquals(mapOf("key" to "value"), behavior.exporter?.http?.headers)
    }
}
