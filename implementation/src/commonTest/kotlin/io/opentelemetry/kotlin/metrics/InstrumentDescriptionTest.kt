package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class InstrumentDescriptionTest {

    @Test
    fun nullDescriptionIsUnchanged() {
        val handler = FakeSdkErrorHandler()
        assertNull(handler.sanitizeInstrumentDescription(null))
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun emptyDescriptionIsUnchanged() {
        val handler = FakeSdkErrorHandler()
        assertEquals("", handler.sanitizeInstrumentDescription(""))
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun descriptionAtLimitIsUnchanged() {
        val handler = FakeSdkErrorHandler()
        val description = "a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS)
        assertEquals(description, handler.sanitizeInstrumentDescription(description))
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun descriptionOverLimitIsTruncatedAndReported() {
        val handler = FakeSdkErrorHandler()
        val description = "a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS + 1)
        val sanitized = handler.sanitizeInstrumentDescription(description)
        assertEquals("a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS), sanitized)
        assertEquals(1, handler.apiMisuses.size)
    }

    @Test
    fun supplementaryPlaneCharacterWithinLimitIsKept() {
        val handler = FakeSdkErrorHandler()
        // U+1F600 GRINNING FACE is a surrogate pair, one code point but two UTF-16 chars.
        val emoji = "\uD83D\uDE00"
        val description = emoji + "a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS - 1)
        assertEquals(description, handler.sanitizeInstrumentDescription(description))
        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun supplementaryPlaneCharacterIsNotSplitWhenTruncating() {
        val handler = FakeSdkErrorHandler()
        val emoji = "\uD83D\uDE00"
        // Surrogate pair sits right at the boundary; truncation must drop it whole, not split it.
        val description = "a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS) + emoji
        val sanitized = handler.sanitizeInstrumentDescription(description)
        assertEquals("a".repeat(MAX_INSTRUMENT_DESCRIPTION_CHARS), sanitized)
        assertEquals(1, handler.apiMisuses.size)
    }
}
