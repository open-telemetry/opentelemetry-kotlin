package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchProcessorDelegateTest {
    @Test
    fun batchSizeIsCappedAtQueueSize() {
        val delegate = BatchProcessorDelegate(
            maxQueueSize = 100,
            requestedQueueSize = 200,
        )
        assertEquals(100, delegate.maxExportBatchSize)
    }

    @Test
    fun defaultBatchSizeIsCappedAtQueueSize() {
        val delegate = BatchProcessorDelegate(
            maxQueueSize = 100,
        )
        assertEquals(100, delegate.maxExportBatchSize)
    }
}
