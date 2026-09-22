package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorBehaviorTest {

    @Test
    fun exporterStartsUnset() {
        assertNull(LogRecordProcessorBehavior().exporter)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredExporter() {
        assertNull(LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior()).exporter)
    }

    @Test
    fun adoptsExporterFromWhicheverLayerSuppliedIt() {
        val exporter = LogExporterBehavior.Console

        assertEquals(
            exporter,
            LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior(exporter = exporter)).exporter,
        )
        assertEquals(
            exporter,
            LogRecordProcessorBehavior(exporter = exporter).mergeWith(LogRecordProcessorBehavior()).exporter,
        )
    }
}
