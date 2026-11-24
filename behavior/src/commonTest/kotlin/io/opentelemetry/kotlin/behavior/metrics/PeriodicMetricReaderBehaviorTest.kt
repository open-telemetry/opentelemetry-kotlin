package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PeriodicMetricReaderBehaviorTest {

    @Test
    fun everyFieldStartsUnset() {
        val reader = PeriodicMetricReaderBehavior()

        assertNull(reader.interval)
        assertNull(reader.timeout)
        assertNull(reader.maxExportBatchSize)
        assertNull(reader.exporter)
    }

    @Test
    fun adoptsEverythingWhenLowerIsUnset() {
        val higher = PeriodicMetricReaderBehavior(
            interval = 1_000,
            timeout = 500,
            maxExportBatchSize = 100,
            exporter = PushMetricExporterBehavior(console = ConsoleExporterBehavior()),
        )

        assertEquals(higher, PeriodicMetricReaderBehavior().mergeWith(higher))
    }

    @Test
    fun partialHigherLayerPreservesOtherFields() {
        val lower = PeriodicMetricReaderBehavior(
            interval = 1_000,
            timeout = 500,
            maxExportBatchSize = 100,
            exporter = PushMetricExporterBehavior(console = ConsoleExporterBehavior()),
        )
        val higher = PeriodicMetricReaderBehavior(interval = 15_000)

        val merged = lower.mergeWith(higher)

        assertEquals(15_000, merged.interval)
        assertEquals(500, merged.timeout)
        assertEquals(100, merged.maxExportBatchSize)
        assertEquals(lower.exporter, merged.exporter)
    }

    @Test
    fun prefersHigherLayerForEveryField() {
        val lower = PeriodicMetricReaderBehavior(
            interval = 1_000,
            timeout = 500,
            maxExportBatchSize = 100,
            exporter = PushMetricExporterBehavior(),
        )
        val higher = PeriodicMetricReaderBehavior(
            interval = 2_000,
            timeout = 0,
            maxExportBatchSize = 50,
            exporter = PushMetricExporterBehavior(console = ConsoleExporterBehavior()),
        )

        assertEquals(higher, lower.mergeWith(higher))
    }
}
