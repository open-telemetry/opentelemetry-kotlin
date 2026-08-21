package io.opentelemetry.kotlin.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class GuardTest {

    private val handler = FakeSdkErrorHandler()

    @Test
    fun testGuardSuccessReportsNothing() {
        var invoked = false
        handler.guard("should not be used") { invoked = true }

        assertTrue(invoked)
        assertFalse(handler.hasErrors())
    }

    @Test
    fun testGuardReportsThrowable() {
        val cause = IllegalStateException("boom")
        handler.guard("SpanProcessor.onStart failed") { throw cause }

        val error = handler.userCodeErrors.single()
        assertSame(cause, error.cause)
        assertEquals("SpanProcessor.onStart failed", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }
}
