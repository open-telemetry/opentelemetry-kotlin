package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SafeExceptionAttributesCommonTest {

    @Test
    fun fallbackBoundsUtf8MessagesAndBuildsBoundedSummary() {
        val attributes = safeExceptionAttributesFallback(IllegalStateException("€".repeat(1024)))

        assertEquals(341, attributes.message?.length)
        assertEquals("${IllegalStateException::class.simpleName}: ${"€".repeat(341)}", attributes.stackTrace)
        assertEquals("😀".repeat(256), truncateUtf8("😀".repeat(257)))
    }

    @Test
    fun fallbackToleratesHostileMessageAccessor() {
        val attributes = safeExceptionAttributesFallback(HostileMessageThrowable())

        assertEquals(HostileMessageThrowable::class.simpleName, attributes.message)
        assertEquals(HostileMessageThrowable::class.simpleName, attributes.stackTrace)
    }

    @Suppress("ExceptionRaisedInUnexpectedLocation")
    private class HostileMessageThrowable : Throwable() {
        override val message: String
            get() = error("message accessed")
    }
}
