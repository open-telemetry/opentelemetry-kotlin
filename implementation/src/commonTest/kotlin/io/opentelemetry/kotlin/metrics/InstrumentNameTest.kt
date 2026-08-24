package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InstrumentNameTest {

    @Test
    fun validNamesAreAcceptedWithoutError() {
        val handler = FakeSdkErrorHandler()
        val validNames = listOf(
            "a",
            "queue.depth",
            "http/server/duration",
            "A-b_c.d/e1",
            "a".repeat(MAX_INSTRUMENT_NAME_CHARS),
        )
        for (name in validNames) {
            assertTrue(handler.isValidInstrumentName(name), "expected \"$name\" to be valid")
        }
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun invalidNamesAreRejectedAndReported() {
        val invalidNames = listOf(
            "",
            "1abc",
            "_foo",
            "/foo",
            "foo bar",
            "foo\$",
            "a".repeat(MAX_INSTRUMENT_NAME_CHARS + 1),
            "café",
        )
        for (name in invalidNames) {
            val handler = FakeSdkErrorHandler()
            assertTrue(!handler.isValidInstrumentName(name), "expected \"$name\" to be invalid")
            assertEquals(1, handler.apiMisuses.size)
            assertEquals("Instrument.name", handler.apiMisuses.single().api)
        }
    }
}
