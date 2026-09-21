package io.opentelemetry.kotlin.error

import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun testReportErrorForwardsApiMisuse() {
        val error = SdkError.ApiMisuse("TestApi", "boom", SdkErrorSeverity.WARNING)

        handler.reportError(error)

        assertSame(error, handler.apiMisuses.single())
    }

    @Test
    fun testReportErrorSwallowsThrowingHandler() {
        val throwingHandler = SdkErrorHandler { throw IllegalStateException("boom") }

        throwingHandler.reportError(
            SdkError.ApiMisuse("TestApi", "boom", SdkErrorSeverity.WARNING)
        )
    }

    @Test
    fun testGuardRethrowsCancellationException() {
        val cancelled = CancellationException("cancelled")
        val thrown = assertFailsWith<CancellationException> {
            handler.guard("should not be used") { throw cancelled }
        }
        assertSame(cancelled, thrown)
        assertFalse(handler.hasErrors())
    }

    @Test
    fun testGuardOrDefaultRethrowsCancellationException() {
        val cancelled = CancellationException("cancelled")
        val thrown = assertFailsWith<CancellationException> {
            handler.guardOrDefault("default", "should not be used") { throw cancelled }
        }
        assertSame(cancelled, thrown)
        assertFalse(handler.hasErrors())
    }

    @Test
    fun testGuardOrDefaultSuspendRethrowsCancellationException() = runTest {
        val cancelled = CancellationException("cancelled")
        val thrown = assertFailsWith<CancellationException> {
            handler.guardOrDefaultSuspend("default", "should not be used") { throw cancelled }
        }
        assertSame(cancelled, thrown)
        assertFalse(handler.hasErrors())
    }
}
