package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.behavior.tracing.SpanExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

class BatchProcessorDelegateTest {
    @Test
    fun batchSizeIsCappedAtQueueSize() {
        val delegate = BatchProcessorDelegate<SpanExporterBehavior>(
            maxQueueSize = 100,
            requestedQueueSize = 200,
        )
        assertEquals(100, delegate.maxExportBatchSize)
    }

    @Test
    fun defaultBatchSizeIsCappedAtQueueSize() {
        val delegate = BatchProcessorDelegate<SpanExporterBehavior>(
            maxQueueSize = 100,
        )
        assertEquals(100, delegate.maxExportBatchSize)
    }
}
