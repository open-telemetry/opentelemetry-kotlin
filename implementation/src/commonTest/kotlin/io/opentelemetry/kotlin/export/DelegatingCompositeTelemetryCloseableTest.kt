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
internal class DelegatingCompositeTelemetryCloseableTest {

    private lateinit var errorHandler: FakeSdkErrorHandler
    private lateinit var closeable: DelegatingTelemetryCloseable

    @BeforeTest
    fun setUp() {
        errorHandler = FakeSdkErrorHandler()
        closeable = DelegatingTelemetryCloseable(errorHandler)
    }

    @Test
    fun testEmptyForceFlushSuccess() = runTest {
        val result = closeable.forceFlush()
        assertEquals(Success, result)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testEmptyShutdownSuccess() = runTest {
        val result = closeable.shutdown()
        assertEquals(Success, result)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testSingleForceFlushSuccess() = runTest {
        val fake = FakeTelemetryCloseable()
        closeable.add(fake)

        val result = closeable.forceFlush()
        assertEquals(Success, result)
        assertEquals(1, fake.forceFlushCount)
        assertEquals(0, fake.shutdownCount)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testSingleShutdownSuccess() = runTest {
        val fake = FakeTelemetryCloseable()
        closeable.add(fake)

        val result = closeable.shutdown()
        assertEquals(Success, result)
        assertEquals(0, fake.forceFlushCount)
        assertEquals(1, fake.shutdownCount)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testMultiForceFlushSuccess() = runTest {
        val first = FakeTelemetryCloseable()
        val second = FakeTelemetryCloseable()
        closeable.add(first)
        closeable.add(second)

        val result = closeable.forceFlush()
        assertEquals(Success, result)
        assertEquals(1, first.forceFlushCount)
        assertEquals(1, second.forceFlushCount)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testMultiShutdownSuccess() = runTest {
        val first = FakeTelemetryCloseable()
        val second = FakeTelemetryCloseable()
        closeable.add(first)
        closeable.add(second)

        val result = closeable.shutdown()
        assertEquals(Success, result)
        assertEquals(1, first.shutdownCount)
        assertEquals(1, second.shutdownCount)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testPartialForceFlushFailure() = runTest {
        val first = FakeTelemetryCloseable(forceFlushResult = Failure)
        val second = FakeTelemetryCloseable()
        closeable.add(first)
        closeable.add(second)

        val result = closeable.forceFlush()
        assertEquals(Failure, result)
        assertEquals(1, first.forceFlushCount)
        assertEquals(1, second.forceFlushCount)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testPartialShutdownFailure() = runTest {
        val first = FakeTelemetryCloseable(shutdownResult = Failure)
        val second = FakeTelemetryCloseable()
        closeable.add(first)
        closeable.add(second)

        val result = closeable.shutdown()
        assertEquals(Failure, result)
        assertEquals(1, first.shutdownCount)
        assertEquals(1, second.shutdownCount)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testForceFlushThrow() = runTest {
        val first =
            FakeTelemetryCloseable(forceFlushException = IllegalStateException("test error"))
        val second = FakeTelemetryCloseable()
        closeable.add(first)
        closeable.add(second)

        val result = closeable.forceFlush()
        assertEquals(Failure, result)
        assertEquals(1, first.forceFlushCount)
        assertEquals(1, second.forceFlushCount)
        assertTrue(errorHandler.hasErrors())
        assertEquals(1, errorHandler.userCodeErrors.size)
    }

    @Test
    fun testShutdownThrow() = runTest {
        val first = FakeTelemetryCloseable(forceFlushException = IllegalStateException("error 1"))
        val second = FakeTelemetryCloseable(forceFlushException = RuntimeException("error 2"))
        closeable.add(first)
        closeable.add(second)

        val result = closeable.forceFlush()
        assertEquals(Failure, result)
        assertEquals(1, first.forceFlushCount)
        assertEquals(1, second.forceFlushCount)
        assertTrue(errorHandler.hasErrors())
        assertEquals(2, errorHandler.userCodeErrors.size)
    }

    private class FakeTelemetryCloseable(
        private val forceFlushResult: OperationResultCode = Success,
        private val shutdownResult: OperationResultCode = Success,
        private val forceFlushException: Throwable? = null,
        private val shutdownException: Throwable? = null
    ) : TelemetryCloseable {

        var forceFlushCount = 0
        var shutdownCount = 0

        override suspend fun forceFlush(): OperationResultCode {
            forceFlushCount++
            forceFlushException?.let { throw it }
            return forceFlushResult
        }

        override suspend fun shutdown(): OperationResultCode {
            shutdownCount++
            shutdownException?.let { throw it }
            return shutdownResult
        }
    }
}
