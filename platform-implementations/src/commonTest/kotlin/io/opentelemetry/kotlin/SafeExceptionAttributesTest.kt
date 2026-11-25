package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class SafeExceptionAttributesTest {

    @Test
    fun fallbackBoundsUtf8MessageAndBuildsBoundedStackSummary() {
        val attributes = safeExceptionAttributesFallback(IllegalStateException("€".repeat(1024)))

        assertEquals(341, attributes.message?.length)
        assertEquals("${attributes.type}: ${"€".repeat(341)}", attributes.stacktrace)
    }

    @Test
    fun truncationPreservesCompleteCharacters() {
        assertEquals("a".repeat(1024), truncateUtf8("a".repeat(1025)))
        assertEquals("€".repeat(341), truncateUtf8("€".repeat(342)))
        assertEquals("😀".repeat(256), truncateUtf8("😀".repeat(257)))
    }

    @Test
    fun truncationReturnsUnchangedValueWithinLimit() {
        val message = "plain message"

        assertSame(message, truncateUtf8(message))
        assertEquals("é", truncateUtf8("é"))
    }

    @Test
    fun fallbackToleratesHostileMessageAccessor() {
        val attributes = safeExceptionAttributesFallback(HostileMessageThrowable())

        assertEquals(assertNotNull(attributes.type), attributes.message)
        assertEquals(attributes.type, attributes.stacktrace)
    }

    @Suppress("ExceptionRaisedInUnexpectedLocation")
    private class HostileMessageThrowable : Throwable() {
        override val message: String
            get() = error("message accessed")
    }
}
