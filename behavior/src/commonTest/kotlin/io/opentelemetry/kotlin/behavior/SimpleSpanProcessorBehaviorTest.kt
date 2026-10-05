package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class SimpleSpanProcessorBehaviorTest {

    @Test
    fun anyTwoInstancesAreEquivalent() {
        assertEquals(SimpleSpanProcessorBehavior(), SimpleSpanProcessorBehavior())
        assertEquals(
            SimpleSpanProcessorBehavior().hashCode(),
            SimpleSpanProcessorBehavior().hashCode(),
        )
    }

    @Test
    fun isNotEquivalentToOtherBehaviors() {
        assertNotEquals<Any?>(SimpleSpanProcessorBehavior(), ConsoleExporterBehavior())
        assertNotEquals<Any?>(SimpleSpanProcessorBehavior(), null)
    }

    @Test
    fun mergingKeepsTheSelection() {
        val behavior = SimpleSpanProcessorBehavior()

        assertEquals(behavior, behavior.mergeWith(SimpleSpanProcessorBehavior()))
    }
}
