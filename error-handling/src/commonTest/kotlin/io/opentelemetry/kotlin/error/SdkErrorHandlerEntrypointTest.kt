package io.opentelemetry.kotlin.error

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SdkErrorHandlerEntrypointTest {

    private class RecordingHandler : SdkErrorHandler {
        val errors = mutableListOf<SdkError>()
        override fun onError(error: SdkError) {
            errors += error
        }
    }

    private val handler = DEFAULT_SDK_ERROR_HANDLER

    @AfterTest
    fun tearDown() {
        setProcessSdkErrorHandler(null)
    }

    @Test
    fun testReplayInOrder() {
        val first = misuse("first")
        val second = misuse("second")
        handler.onError(first)
        handler.onError(second)

        val delegate = RecordingHandler()
        assertTrue(setProcessSdkErrorHandler(delegate))
        assertEquals(listOf<SdkError>(first, second), delegate.errors)
    }

    @Test
    fun testOverflowReportsDroppedCount() {
        repeat(MAX_BUFFERED_ERRORS + 6) { handler.onError(misuse("$it")) }

        val delegate = RecordingHandler()
        setProcessSdkErrorHandler(delegate)
        assertEquals(MAX_BUFFERED_ERRORS + 1, delegate.errors.size)
        val lastKept = delegate.errors[MAX_BUFFERED_ERRORS - 1] as SdkError.ApiMisuse
        assertEquals("${MAX_BUFFERED_ERRORS - 1}", lastKept.message)
        val summary = delegate.errors.last() as SdkError.ApiMisuse
        assertEquals(SdkErrorSeverity.WARNING, summary.severity)
        assertTrue(summary.message.startsWith("6 errors"))
    }

    @Test
    fun testDelegateReceivesErrorsDirectly() {
        val delegate = RecordingHandler()
        setProcessSdkErrorHandler(delegate)
        val error = misuse("direct")
        handler.onError(error)
        assertEquals(listOf<SdkError>(error), delegate.errors)
    }

    @Test
    fun testSecondDelegateRejected() {
        val first = RecordingHandler()
        val second = RecordingHandler()
        assertTrue(setProcessSdkErrorHandler(first))
        assertFalse(setProcessSdkErrorHandler(second))

        handler.onError(misuse("x"))
        assertEquals(1, first.errors.size)
        assertEquals(0, second.errors.size)
    }

    @Test
    fun testThrowingDelegateDoesNotEscape() {
        handler.onError(misuse("buffered"))
        assertTrue(setProcessSdkErrorHandler { error("boom") })
        handler.onError(misuse("direct"))
    }

    @Test
    fun testDelegateFailureReportedOnce() {
        val reported = mutableListOf<SdkError>()
        val delegate = failingDelegate(reported)
        handler.onError(misuse("buffered"))
        assertTrue(setProcessSdkErrorHandler(delegate))
        handler.onError(misuse("direct"))

        val failure = reported.single() as SdkError.UserCodeError
        assertEquals("boom", failure.cause.message)
        assertEquals(SdkErrorSeverity.WARNING, failure.severity)
    }

    @Test
    fun testDelegateThrowingOnItsOwnFailureDoesNotEscape() {
        var calls = 0
        assertTrue(
            setProcessSdkErrorHandler {
                calls++
                error("boom")
            }
        )
        handler.onError(misuse("first"))
        handler.onError(misuse("second"))
        assertEquals(3, calls)
    }

    @Test
    fun testFailureReportingResetsWithNewDelegate() {
        val reported = mutableListOf<SdkError>()
        val failing = failingDelegate(reported)
        setProcessSdkErrorHandler(failing)
        handler.onError(misuse("x"))
        setProcessSdkErrorHandler(null)
        setProcessSdkErrorHandler(failing)
        handler.onError(misuse("y"))
        assertEquals(2, reported.size)
    }

    @Test
    fun testReentrantReportIsDropped() {
        val received = mutableListOf<SdkError>()
        val delegate = SdkErrorHandler { error ->
            received += error
            handler.onError(misuse("nested"))
        }
        handler.onError(misuse("buffered"))
        assertTrue(setProcessSdkErrorHandler(delegate))
        handler.onError(misuse("direct"))

        assertEquals(listOf("buffered", "direct"), received.map { (it as SdkError.ApiMisuse).message })
    }

    @Test
    fun testReportingWorksAfterReentrantDelegate() {
        val received = mutableListOf<SdkError>()
        setProcessSdkErrorHandler { error ->
            received += error
            handler.onError(misuse("nested"))
        }
        handler.onError(misuse("first"))
        handler.onError(misuse("second"))
        assertEquals(2, received.size)
    }

    @Test
    fun testNullDiscardsBufferAndDelegate() {
        handler.onError(misuse("discarded"))
        assertTrue(setProcessSdkErrorHandler(null))

        val delegate = RecordingHandler()
        assertTrue(setProcessSdkErrorHandler(delegate))
        assertEquals(0, delegate.errors.size)

        assertTrue(setProcessSdkErrorHandler(null))
        assertTrue(setProcessSdkErrorHandler(RecordingHandler()))
    }

    @Test
    fun testReplayContinuesAfterDelegateThrows() {
        repeat(MAX_BUFFERED_ERRORS + 1) { handler.onError(misuse("$it")) }

        val received = mutableListOf<SdkError>()
        assertTrue(
            setProcessSdkErrorHandler { error ->
                received += error
                if (error is SdkError.ApiMisuse && error.message == "1") {
                    error("boom")
                }
            }
        )
        val misuses = received.filterIsInstance<SdkError.ApiMisuse>()
        assertEquals(MAX_BUFFERED_ERRORS + 1, misuses.size) // every kept error plus the dropped summary
        assertEquals("${MAX_BUFFERED_ERRORS - 1}", misuses[MAX_BUFFERED_ERRORS - 1].message)
        assertTrue(misuses.last().message.startsWith("1 errors"))
        assertEquals(1, received.filterIsInstance<SdkError.UserCodeError>().size)
    }

    @Test
    fun testSetFromInsideDelegateIsRejected() {
        var nestedResult: Boolean? = null
        val other = RecordingHandler()
        setProcessSdkErrorHandler {
            if (nestedResult == null) {
                setProcessSdkErrorHandler(null)
                nestedResult = setProcessSdkErrorHandler(other)
            }
        }
        handler.onError(misuse("trigger"))
        assertEquals(false, nestedResult)

        // the nested null cleared the delegate, so errors buffer and replay to the next delegate
        val buffered = misuse("buffered")
        handler.onError(buffered)
        assertTrue(setProcessSdkErrorHandler(other))
        assertEquals(listOf<SdkError>(buffered), other.errors)
    }

    private fun failingDelegate(reported: MutableList<SdkError>) = SdkErrorHandler { error ->
        if (error is SdkError.UserCodeError) {
            reported += error
        } else {
            error("boom")
        }
    }

    private fun misuse(message: String) = SdkError.ApiMisuse(
        api = "test",
        message = message,
        severity = SdkErrorSeverity.WARNING,
    )
}
