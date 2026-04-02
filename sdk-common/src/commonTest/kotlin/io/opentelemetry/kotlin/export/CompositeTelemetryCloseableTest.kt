package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompositeTelemetryCloseableTest {

    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        errorHandler = FakeSdkErrorHandler()
    }

    private fun composite(vararg c: FakeCloseable) =
        CompositeTelemetryCloseable(c.toList(), errorHandler)

    @Test
    fun testEmptyFlushSucceeds() = runTest {
        assertEquals(Success, CompositeTelemetryCloseable(emptyList(), errorHandler).forceFlush())
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testEmptyShutdownSucceeds() = runTest {
        assertEquals(Success, CompositeTelemetryCloseable(emptyList(), errorHandler).shutdown())
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testAllFlushSucceeds() = runTest {
        val a = FakeCloseable()
        val b = FakeCloseable()
        assertEquals(Success, composite(a, b).forceFlush())
        assertFalse(errorHandler.hasErrors())
        assertEquals(1, a.flushes)
        assertEquals(1, b.flushes)
    }

    @Test
    fun testAllShutdownSucceeds() = runTest {
        val a = FakeCloseable()
        val b = FakeCloseable()
        assertEquals(Success, composite(a, b).shutdown())
        assertFalse(errorHandler.hasErrors())
        assertEquals(1, a.shutdowns)
        assertEquals(1, b.shutdowns)
    }

    @Test
    fun testOneFailFlushReturnsFailure() = runTest {
        assertEquals(
            Failure,
            composite(FakeCloseable(flushResult = Failure), FakeCloseable()).forceFlush()
        )
    }

    @Test
    fun testOneFailShutdownReturnsFailure() = runTest {
        assertEquals(
            Failure,
            composite(FakeCloseable(shutdownResult = Failure), FakeCloseable()).shutdown()
        )
    }

    @Test
    fun testAllCalledEvenOnFlushFailure() = runTest {
        val a = FakeCloseable(flushResult = Failure)
        val b = FakeCloseable()
        composite(a, b).forceFlush()
        assertEquals(1, a.flushes)
        assertEquals(1, b.flushes)
    }

    @Test
    fun testAllCalledEvenOnShutdownFailure() = runTest {
        val a = FakeCloseable(shutdownResult = Failure)
        val b = FakeCloseable()
        composite(a, b).shutdown()
        assertEquals(1, a.shutdowns)
        assertEquals(1, b.shutdowns)
    }

    @Test
    fun testFlushExceptionReportsError() = runTest {
        val b = FakeCloseable()
        assertEquals(
            Failure,
            composite(FakeCloseable(flushEx = IllegalStateException()), b).forceFlush()
        )
        assertTrue(errorHandler.hasErrors())
        assertEquals(1, b.flushes)
    }

    @Test
    fun testShutdownExceptionReportsError() = runTest {
        val b = FakeCloseable()
        assertEquals(
            Failure,
            composite(FakeCloseable(shutdownEx = IllegalStateException()), b).shutdown()
        )
        assertTrue(errorHandler.hasErrors())
        assertEquals(1, b.shutdowns)
    }

    private class FakeCloseable(
        private val flushResult: OperationResultCode = Success,
        private val shutdownResult: OperationResultCode = Success,
        private val flushEx: Throwable? = null,
        private val shutdownEx: Throwable? = null,
    ) : TelemetryCloseable {
        var flushes = 0
        var shutdowns = 0

        override suspend fun forceFlush(): OperationResultCode {
            flushes++
            flushEx?.let { throw it }
            return flushResult
        }

        override suspend fun shutdown(): OperationResultCode {
            shutdowns++
            shutdownEx?.let { throw it }
            return shutdownResult
        }
    }
}
