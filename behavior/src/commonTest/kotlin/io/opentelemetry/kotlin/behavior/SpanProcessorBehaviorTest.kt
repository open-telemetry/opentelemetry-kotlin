package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanProcessorBehaviorTest {

    @Test
    fun consoleStartsUnset() {
        assertNull(SpanProcessorBehavior().console)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredConsole() {
        assertNull(SpanProcessorBehavior().mergeWith(SpanProcessorBehavior()).console)
    }

    @Test
    fun adoptsConsoleFromWhicheverLayerSuppliedIt() {
        val console = ConsoleExporterBehavior()

        assertEquals(
            console,
            SpanProcessorBehavior().mergeWith(SpanProcessorBehavior(console = console)).console,
        )
        assertEquals(
            console,
            SpanProcessorBehavior(console = console).mergeWith(SpanProcessorBehavior()).console,
        )
    }
}
