package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class InstrumentUnitTest {

    @Test
    fun nullUnitIsUnchanged() {
        val handler = FakeSdkErrorHandler()
        assertNull(handler.sanitizeInstrumentUnit(null))
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun validUnitsAreUnchangedWithoutError() {
        val handler = FakeSdkErrorHandler()
        val validUnits = listOf("", "{item}", "ms", "kB", "a".repeat(MAX_INSTRUMENT_UNIT_CHARS))
        for (unit in validUnits) {
            assertEquals(unit, handler.sanitizeInstrumentUnit(unit), "expected \"$unit\" to be valid")
        }
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun unitOverLimitIsDroppedAndReported() {
        val handler = FakeSdkErrorHandler()
        val unit = "a".repeat(MAX_INSTRUMENT_UNIT_CHARS + 1)
        assertNull(handler.sanitizeInstrumentUnit(unit))
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("Instrument.unit", handler.apiMisuses.single().api)
    }

    @Test
    fun nonAsciiUnitIsDroppedAndReported() {
        val handler = FakeSdkErrorHandler()
        assertNull(handler.sanitizeInstrumentUnit("café"))
        assertEquals(1, handler.apiMisuses.size)
        assertEquals("Instrument.unit", handler.apiMisuses.single().api)
    }
}
