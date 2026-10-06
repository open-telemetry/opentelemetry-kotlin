package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class CompletableResultCodeExtTest {

    @Test
    fun `test completed results`() = runTest {
        assertEquals(
            OperationResultCode.Success,
            OtelJavaCompletableResultCode.ofSuccess().toOperationResultCode()
        )
        assertEquals(
            OperationResultCode.Failure,
            OtelJavaCompletableResultCode.ofFailure().toOperationResultCode()
        )
    }

    @Test
    fun `test exceptional failure rethrows its throwable`() = runTest {
        val thrown = assertFailsWith<IllegalStateException> {
            OtelJavaCompletableResultCode.ofExceptionalFailure(IllegalStateException("boom"))
                .toOperationResultCode()
        }
        assertEquals("boom", thrown.message)
    }

    @Test
    fun `test result that fails exceptionally asynchronously`() = runTest {
        val pending = OtelJavaCompletableResultCode()
        val result = async { runCatching { pending.toOperationResultCode() } }
        runCurrent()

        pending.failExceptionally(IllegalStateException("boom"))
        assertIs<IllegalStateException>(result.await().exceptionOrNull())
    }

    @Test
    fun `test results that are not the shared singletons`() = runTest {
        assertEquals(
            OperationResultCode.Success,
            OtelJavaCompletableResultCode().succeed().toOperationResultCode()
        )
        assertEquals(
            OperationResultCode.Failure,
            OtelJavaCompletableResultCode().fail().toOperationResultCode()
        )
    }

    @Test
    fun `test result that succeeds asynchronously`() = runTest {
        val pending = OtelJavaCompletableResultCode()
        val result = async { pending.toOperationResultCode() }
        runCurrent()

        pending.succeed()
        assertEquals(OperationResultCode.Success, result.await())
    }

    @Test
    fun `test result that fails asynchronously`() = runTest {
        val pending = OtelJavaCompletableResultCode()
        val result = async { pending.toOperationResultCode() }
        runCurrent()

        pending.fail()
        assertEquals(OperationResultCode.Failure, result.await())
    }

    @Test
    fun `test result that never completes is cancellable`() = runTest {
        val pending = OtelJavaCompletableResultCode()
        assertNull(
            withTimeoutOrNull(TIMEOUT_MS) { pending.toOperationResultCode() }
        )
    }

    @Test
    fun `test exception thrown by wrapped component propagates`() = runTest {
        assertFailsWith<IllegalStateException> {
            awaitOperationResultCode { throw IllegalStateException("boom") }
        }
    }

    @Test
    fun `test await delegates to the supplied result`() = runTest {
        assertEquals(
            OperationResultCode.Success,
            awaitOperationResultCode { OtelJavaCompletableResultCode().succeed() }
        )
    }

    @Test
    fun `test await times out if the result never completes`() = runTest {
        assertEquals(
            OperationResultCode.Failure,
            awaitOperationResultCode { OtelJavaCompletableResultCode() }
        )
        assertEquals(COMPAT_DEFAULT_TIMEOUT_MS, testScheduler.currentTime)
    }

    @Test
    fun `test await honours an explicit timeout`() = runTest {
        assertEquals(
            OperationResultCode.Failure,
            awaitOperationResultCode(TIMEOUT_MS) { OtelJavaCompletableResultCode() }
        )
        assertEquals(TIMEOUT_MS, testScheduler.currentTime)
    }

    @Test
    fun `test launch completes with the action result`() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val success = launchAsCompletableResultCode(errorHandler, "test") { OperationResultCode.Success }
        val failure = launchAsCompletableResultCode(errorHandler, "test") { OperationResultCode.Failure }
        advanceUntilIdle()

        assertTrue(success.isSuccess)
        assertTrue(failure.isDone)
        assertFalse(failure.isSuccess)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun `test launch reports exceptions and fails`() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val result = launchAsCompletableResultCode(errorHandler, "test") { error("boom") }
        advanceUntilIdle()

        assertTrue(result.isDone)
        assertFalse(result.isSuccess)
        assertEquals("test", errorHandler.userCodeErrors.single().message)
    }

    @Test
    fun `test launch fails on timeout`() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val result = launchAsCompletableResultCode(errorHandler, "test", TIMEOUT_MS) {
            awaitCancellation()
        }
        advanceUntilIdle()

        assertTrue(result.isDone)
        assertFalse(result.isSuccess)
        assertEquals(TIMEOUT_MS, testScheduler.currentTime)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun `test launch on a cancelled scope fails`() {
        var called = false
        val scope = CoroutineScope(Job().apply { cancel() })
        val result = scope.launchAsCompletableResultCode(FakeSdkErrorHandler(), "test") {
            called = true
            OperationResultCode.Success
        }.join(TIMEOUT_MS, TimeUnit.MILLISECONDS)

        assertTrue(result.isDone)
        assertFalse(result.isSuccess)
        assertFalse(called)
    }

    private companion object {
        const val TIMEOUT_MS = 1000L
    }
}
