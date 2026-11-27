package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TimeoutOperationTest {

    @Test
    fun testReturnsActionResultWhenItCompletesInTime() = runTest {
        assertEquals(OperationResultCode.Success, runWithTimeout(10) { OperationResultCode.Success })
        assertEquals(OperationResultCode.Failure, runWithTimeout(10) { OperationResultCode.Failure })
    }

    @Test
    fun testTimeoutIsReturnedWhenTheTimeoutElapses() = runTest {
        val result = runWithTimeout(10) {
            delay(20)
            OperationResultCode.Success
        }
        assertEquals(OperationResultCode.Timeout, result)
    }

    @Test
    fun testTimeoutIsAFailure() {
        assertIs<OperationResultCode.Failure>(OperationResultCode.Timeout)
        // widen to OperationResultCode: Failure's companion and Timeout are distinct object types
        assertNotEquals<OperationResultCode>(OperationResultCode.Failure, OperationResultCode.Timeout)
    }

    @Test
    fun testShutdownTimeoutIsDistinguishedFromFailure() = runTest {
        val state = MutableShutdownState()
        val result = state.shutdown(10) {
            delay(20)
            OperationResultCode.Success
        }
        assertEquals(OperationResultCode.Timeout, result)
        assertTrue(state.isShutdown)
    }

    @Test
    fun testCompositeFlushReportsTimeoutOverGenericFailure() = runTest {
        val composite = CompositeTelemetryCloseable(
            listOf(FailingCloseable, HangingCloseable),
            FakeSdkErrorHandler(),
        )
        assertEquals(OperationResultCode.Timeout, composite.forceFlush())
    }

    private object FailingCloseable : TelemetryCloseable {
        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Failure
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Failure
    }

    private object HangingCloseable : TelemetryCloseable {
        override suspend fun forceFlush(): OperationResultCode {
            delay(6000)
            return OperationResultCode.Success
        }

        override suspend fun shutdown(): OperationResultCode {
            delay(6000)
            return OperationResultCode.Success
        }
    }
}
