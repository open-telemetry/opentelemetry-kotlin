package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorBehaviorTest {

    @Test
    fun consoleStartsUnset() {
        assertNull(LogRecordProcessorBehavior().console)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredConsole() {
        assertNull(LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior()).console)
    }

    @Test
    fun adoptsConsoleFromWhicheverLayerSuppliedIt() {
        val console = ConsoleExporterBehavior()

        assertEquals(
            console,
            LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior(console = console)).console,
        )
        assertEquals(
            console,
            LogRecordProcessorBehavior(console = console).mergeWith(LogRecordProcessorBehavior()).console,
        )
    }
}
