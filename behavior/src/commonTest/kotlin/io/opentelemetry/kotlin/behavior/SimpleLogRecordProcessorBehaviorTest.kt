package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class SimpleLogRecordProcessorBehaviorTest {

    @Test
    fun anyTwoInstancesWithoutAnExporterAreEquivalent() {
        assertEquals(SimpleLogRecordProcessorBehavior(), SimpleLogRecordProcessorBehavior())
        assertEquals(
            SimpleLogRecordProcessorBehavior().hashCode(),
            SimpleLogRecordProcessorBehavior().hashCode(),
        )
    }

    @Test
    fun isNotEquivalentToOtherBehaviors() {
        assertNotEquals<Any?>(SimpleLogRecordProcessorBehavior(), ConsoleExporterBehavior())
        assertNotEquals<Any?>(SimpleLogRecordProcessorBehavior(), null)
    }

    @Test
    fun mergingKeepsTheSelection() {
        val behavior = SimpleLogRecordProcessorBehavior()

        assertEquals(behavior, behavior.mergeWith(SimpleLogRecordProcessorBehavior()))
    }

    @Test
    fun mergesExporterAcrossLayers() {
        val console = ConsoleExporterBehavior()
        val http = OtlpHttpExporterBehavior(endpoint = "https://example.com")

        val merged = SimpleLogRecordProcessorBehavior(LogRecordExporterBehavior(console = console))
            .mergeWith(SimpleLogRecordProcessorBehavior(LogRecordExporterBehavior(http = http)))

        assertEquals(
            SimpleLogRecordProcessorBehavior(LogRecordExporterBehavior(console = console, http = http)),
            merged,
        )
    }
}
