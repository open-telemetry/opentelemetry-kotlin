package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorBehaviorTest {

    @Test
    fun batchStartsUnsetAndRemainsUnsetWhenNeitherLayerConfiguresIt() {
        val empty = LogRecordProcessorBehavior()

        assertNull(empty.batch)
        assertNull(empty.mergeWith(empty).batch)
    }

    @Test
    fun adoptsBatchFromWhicheverLayerSuppliedIt() {
        val batch = BatchLogRecordProcessorBehavior(scheduleDelay = 1_000)
        val configured = LogRecordProcessorBehavior(batch = batch)
        val empty = LogRecordProcessorBehavior()

        assertEquals(configured, configured.mergeWith(empty))
        assertEquals(configured, empty.mergeWith(configured))
    }

    @Test
    fun mergesBatchSettingsWithoutDroppingExporterConfiguration() {
        val http = OtlpHttpExporterBehavior(endpoint = "https://example.com")
        val merged = LogRecordProcessorBehavior(
            http = http,
            batch = BatchLogRecordProcessorBehavior(scheduleDelay = 1_000, maxQueueSize = 2_048),
        ).mergeWith(
            LogRecordProcessorBehavior(batch = BatchLogRecordProcessorBehavior(scheduleDelay = 2_000)),
        )

        assertEquals(http, merged.http)
        assertEquals(BatchLogRecordProcessorBehavior(scheduleDelay = 2_000, maxQueueSize = 2_048), merged.batch)
    }

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

    @Test
    fun httpStartsUnset() {
        assertNull(LogRecordProcessorBehavior().http)
    }

    @Test
    fun httpStaysUnsetWhenNeitherLayerConfigured() {
        assertNull(LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior()).http)
    }

    @Test
    fun adoptsHttpFromWhicheverLayerSuppliedIt() {
        val http = OtlpHttpExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
        )

        assertEquals(
            http,
            LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior(http = http)).http,
        )
        assertEquals(
            http,
            LogRecordProcessorBehavior(http = http).mergeWith(LogRecordProcessorBehavior()).http,
        )
    }

    @Test
    fun simpleStartsUnset() {
        assertNull(LogRecordProcessorBehavior().simple)
    }

    @Test
    fun simpleStaysUnsetWhenNeitherLayerConfigured() {
        assertNull(LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior()).simple)
    }

    @Test
    fun adoptsSimpleFromWhicheverLayerSuppliedIt() {
        val simple = SimpleLogRecordProcessorBehavior()

        assertEquals(
            simple,
            LogRecordProcessorBehavior().mergeWith(LogRecordProcessorBehavior(simple = simple)).simple,
        )
        assertEquals(
            simple,
            LogRecordProcessorBehavior(simple = simple).mergeWith(LogRecordProcessorBehavior()).simple,
        )
    }

    @Test
    fun keepsExporterConfigurationWhenSelectingSimple() {
        val http = OtlpHttpExporterBehavior(endpoint = "https://example.com")
        val merged = LogRecordProcessorBehavior(http = http).mergeWith(
            LogRecordProcessorBehavior(simple = SimpleLogRecordProcessorBehavior()),
        )

        assertEquals(http, merged.http)
        assertEquals(SimpleLogRecordProcessorBehavior(), merged.simple)
    }
}
