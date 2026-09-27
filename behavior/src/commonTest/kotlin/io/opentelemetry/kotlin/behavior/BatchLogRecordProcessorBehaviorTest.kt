package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class BatchLogRecordProcessorBehaviorTest {

    private val configured = BatchLogRecordProcessorBehavior(
        scheduleDelay = 1_000,
        exportTimeout = 30_000,
        maxQueueSize = 2_048,
        maxExportBatchSize = 512,
    )

    @Test
    fun everyFieldStartsUnset() {
        val behavior = BatchLogRecordProcessorBehavior()

        assertNull(behavior.scheduleDelay)
        assertNull(behavior.exportTimeout)
        assertNull(behavior.maxQueueSize)
        assertNull(behavior.maxExportBatchSize)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguresAnything() {
        val empty = BatchLogRecordProcessorBehavior()

        assertEquals(empty, empty.mergeWith(empty))
    }

    @Test
    fun adoptsValuesFromWhicheverLayerSuppliedThem() {
        val empty = BatchLogRecordProcessorBehavior()

        assertEquals(configured, empty.mergeWith(configured))
        assertEquals(configured, configured.mergeWith(empty))
    }

    @Test
    fun higherLayerOverridesEveryConfiguredField() {
        val higher = BatchLogRecordProcessorBehavior(
            scheduleDelay = 2_000,
            exportTimeout = 60_000,
            maxQueueSize = 4_096,
            maxExportBatchSize = 1_024,
        )

        assertEquals(higher, configured.mergeWith(higher))
    }

    @Test
    fun partialHigherLayerPreservesOtherFields() {
        val higher = BatchLogRecordProcessorBehavior(exportTimeout = 60_000)

        assertEquals(configured.copy(exportTimeout = 60_000), configured.mergeWith(higher))
        assertEquals(BatchLogRecordProcessorBehavior(exportTimeout = 60_000), higher)
        assertEquals(30_000L, configured.exportTimeout)
    }

    @Test
    fun zeroDurationsAreConfiguredValues() {
        val higher = BatchLogRecordProcessorBehavior(scheduleDelay = 0, exportTimeout = 0)

        assertEquals(configured.copy(scheduleDelay = 0, exportTimeout = 0), configured.mergeWith(higher))
    }
}
