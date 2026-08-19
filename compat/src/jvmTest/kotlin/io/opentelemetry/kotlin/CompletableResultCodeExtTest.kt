package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.export.OperationResultCode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
        assertEquals(
            OperationResultCode.Failure,
            OtelJavaCompletableResultCode.ofExceptionalFailure(RuntimeException())
                .toOperationResultCode()
        )
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
    fun `test exception thrown by wrapped component`() = runTest {
        assertEquals(
            OperationResultCode.Failure,
            awaitOperationResultCode { throw IllegalStateException("boom") }
        )
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

    private companion object {
        const val TIMEOUT_MS = 1000L
    }
}
