package io.opentelemetry.kotlin.error

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
    fun testGuardReportsCancellationException() {
        val cancelled = CancellationException("cancelled")
        handler.guard("SpanProcessor.onEnd failed") { throw cancelled }

        val error = handler.userCodeErrors.single()
        assertSame(cancelled, error.cause)
        assertEquals("SpanProcessor.onEnd failed", error.message)
    }

    @Test
    fun testGuardOrDefaultReportsCancellationException() {
        val cancelled = CancellationException("cancelled")
        val result = handler.guardOrDefault("default", "Tracer.startSpan failed") { throw cancelled }

        assertEquals("default", result)
        assertSame(cancelled, handler.userCodeErrors.single().cause)
    }

    @Test
    fun testGuardOrDefaultSuspendReportsCancellationExceptionWhenCallerActive() = runTest {
        val cancelled = CancellationException("cancelled")
        val result = handler.guardOrDefaultSuspend("default", "Exporter.export failed") { throw cancelled }

        assertEquals("default", result)
        assertSame(cancelled, handler.userCodeErrors.single().cause)
    }

    @Test
    fun testGuardOrDefaultSuspendRethrowsWhenCallerCancelled() = runTest {
        var result: String? = null
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            result = handler.guardOrDefaultSuspend("default", "should not be used") { awaitCancellation() }
        }
        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertNull(result)
        assertFalse(handler.hasErrors())
    }

    @Test
    fun testSdkGuardReportsSdkCodeError() {
        val cause = IllegalStateException("boom")
        handler.sdkGuard("Span.end failed") { throw cause }

        val error = handler.sdkCodeErrors.single()
        assertSame(cause, error.cause)
        assertEquals("Span.end failed", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals(1, handler.errors.size)
    }

    @Test
    fun testSdkGuardReportsUserCodeAsUserCodeError() {
        val cause = IllegalStateException("boom")
        var reachedAfterUserCode = false
        handler.sdkGuard("Tracer.startSpan failed") {
            userCode { throw cause }
            reachedAfterUserCode = true
        }
        assertFalse(reachedAfterUserCode)
        val error = handler.userCodeErrors.single()
        assertSame(cause, error.cause)
        assertEquals("Tracer.startSpan failed", error.message)
        assertEquals(1, handler.errors.size)
    }

    @Test
    fun testNestedUserCodeIsReportedOnce() {
        val cause = IllegalStateException("boom")
        handler.sdkGuard("Tracer.startSpan failed") {
            userCode { userCode { throw cause } }
        }
        assertSame(cause, handler.userCodeErrors.single().cause)
        assertEquals(1, handler.errors.size)
    }

    @Test
    fun testUserCodeReturnsValue() {
        val result = handler.sdkGuardOrDefault("default", "should not be used") { userCode { "value" } }
        assertEquals("value", result)
        assertFalse(handler.hasErrors())
    }

    @Test
    fun testSdkGuardOrDefaultReportsUserCodeAsUserCodeError() {
        val cause = IllegalStateException("boom")
        val result = handler.sdkGuardOrDefault("default", "Logger.emit failed") { userCode { throw cause } }
        assertEquals("default", result)
        assertSame(cause, handler.userCodeErrors.single().cause)
    }

    @Test
    fun testGuardUnwrapsUserCode() {
        val cause = IllegalStateException("boom")
        handler.guard("SpanProcessor.onStart failed") { userCode { throw cause } }
        assertSame(cause, handler.userCodeErrors.single().cause)
    }

    @Test
    fun testSdkGuardOrDefaultSuspendReportsSdkCodeError() = runTest {
        val cause = IllegalStateException("boom")
        val result = handler.sdkGuardOrDefaultSuspend("default", "TracerProvider.shutdown failed") { throw cause }
        assertEquals("default", result)
        assertSame(cause, handler.sdkCodeErrors.single().cause)
    }

    @Test
    fun testSdkGuardOrDefaultSuspendReportsUserCodeAsUserCodeError() = runTest {
        val cause = IllegalStateException("boom")
        val result = handler.sdkGuardOrDefaultSuspend("default", "TracerProvider.shutdown failed") {
            userCode { throw cause }
        }
        assertEquals("default", result)
        assertSame(cause, handler.userCodeErrors.single().cause)
    }

    @Test
    fun testSdkGuardOrDefaultSuspendRethrowsWhenCallerCancelled() = runTest {
        var result: String? = null
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            result = handler.sdkGuardOrDefaultSuspend("default", "should not be used") { awaitCancellation() }
        }
        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertNull(result)
        assertFalse(handler.hasErrors())
    }
}
