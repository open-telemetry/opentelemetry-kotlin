package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BatchSpanProcessorBehaviorTest {
    @Test
    fun everyFieldStartsUnset() {
        val processor = BatchSpanProcessorBehavior()
        assertNull(processor.scheduleDelay)
        assertNull(processor.exportTimeout)
        assertNull(processor.maxQueueSize)
        assertNull(processor.maxExportBatchSize)
        assertNull(processor.exporter)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = BatchSpanProcessorBehavior(
            scheduleDelay = 100,
            exportTimeout = 200,
            maxQueueSize = 300,
            maxExportBatchSize = 400,
            exporter = SpanExporterBehavior()
        )
        assertEquals(higher, BatchSpanProcessorBehavior().mergeWith(higher))
    }

    @Test
    fun partialHigherLayerPreservesOtherFields() {
        val lower = BatchSpanProcessorBehavior(
            scheduleDelay = 100,
            exportTimeout = 200,
            maxQueueSize = 300,
            maxExportBatchSize = 400,
            exporter = SpanExporterBehavior(console = ConsoleExporterBehavior())
        )
        val higher = BatchSpanProcessorBehavior(scheduleDelay = 9_999)

        val merged = lower.mergeWith(higher)
        assertEquals(merged.scheduleDelay, higher.scheduleDelay)
        assertEquals(merged.exportTimeout, lower.exportTimeout)
        assertEquals(merged.maxQueueSize, lower.maxQueueSize)
        assertEquals(merged.maxExportBatchSize, lower.maxExportBatchSize)
        assertEquals(merged.exporter, lower.exporter)
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = BatchSpanProcessorBehavior(
            scheduleDelay = 100,
            exportTimeout = 200,
            maxQueueSize = 300,
            maxExportBatchSize = 400,
            exporter = SpanExporterBehavior(http = OtlpHttpSpanExporterBehavior(endpoint = "www.example1.com"))
        )
        val higher = BatchSpanProcessorBehavior(
            scheduleDelay = 1_000,
            exportTimeout = 2_000,
            maxQueueSize = 3_000,
            maxExportBatchSize = 4_000,
            exporter = SpanExporterBehavior(http = OtlpHttpSpanExporterBehavior(endpoint = "www.example2.com"))
        )
        assertEquals(higher, lower.mergeWith(higher))
    }
}
