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
    fun consoleStaysUnsetWhenNeitherLayerConfigured() {
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

    @Test
    fun httpStartsUnset() {
        assertNull(SpanProcessorBehavior().http)
    }

    @Test
    fun httpStaysUnsetWhenNeitherLayerConfigured() {
        assertNull(SpanProcessorBehavior().mergeWith(SpanProcessorBehavior()).http)
    }

    @Test
    fun adoptsHttpFromWhicheverLayerSuppliedIt() {
        val http = OtlpHttpExporterBehavior(
            endpoint = "https://example.com",
            timeout = 10_000,
        )

        assertEquals(
            http,
            SpanProcessorBehavior().mergeWith(SpanProcessorBehavior(http = http)).http,
        )
        assertEquals(
            http,
            SpanProcessorBehavior(http = http).mergeWith(SpanProcessorBehavior()).http,
        )
    }

    @Test
    fun simpleStartsUnset() {
        assertNull(SpanProcessorBehavior().simple)
    }

    @Test
    fun simpleStaysUnsetWhenNeitherLayerConfigured() {
        assertNull(SpanProcessorBehavior().mergeWith(SpanProcessorBehavior()).simple)
    }

    @Test
    fun adoptsSimpleFromWhicheverLayerSuppliedIt() {
        val simple = SimpleSpanProcessorBehavior()

        assertEquals(
            simple,
            SpanProcessorBehavior().mergeWith(SpanProcessorBehavior(simple = simple)).simple,
        )
        assertEquals(
            simple,
            SpanProcessorBehavior(simple = simple).mergeWith(SpanProcessorBehavior()).simple,
        )
    }

    @Test
    fun keepsExporterConfigurationWhenSelectingSimple() {
        val http = OtlpHttpExporterBehavior(endpoint = "https://example.com")
        val merged = SpanProcessorBehavior(http = http).mergeWith(
            SpanProcessorBehavior(simple = SimpleSpanProcessorBehavior()),
        )

        assertEquals(http, merged.http)
        assertEquals(SimpleSpanProcessorBehavior(), merged.simple)
    }
}
