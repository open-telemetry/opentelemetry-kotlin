package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.logging.FakeLoggerProvider
import io.opentelemetry.kotlin.logging.LoggerProvider
import io.opentelemetry.kotlin.tracing.FakeTracerProvider
import io.opentelemetry.kotlin.tracing.TracerProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class CloseableOpenTelemetryImplTest {

    @Test
    fun testNoTelemetryCloseables() = runTest {
        val api = createOpenTelemetry(
            tracerProvider = FakeTracerProvider(),
            loggerProvider = FakeLoggerProvider()
        )
        assertEquals(Success, api.forceFlush())
        assertEquals(Success, api.shutdown())
    }

    @Test
    fun testForceFlushSuccess() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(flushResult = Success)
        val loggerProvider = FakeCloseableLoggerProvider(flushResult = Success)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.forceFlush()
        assertEquals(Success, result)
        assertEquals(1, tracerProvider.flushCount)
        assertEquals(1, loggerProvider.flushCount)
    }

    @Test
    fun testShutdownSuccess() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(shutdownResult = Success)
        val loggerProvider = FakeCloseableLoggerProvider(shutdownResult = Success)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.shutdown()
        assertEquals(Success, result)
        assertEquals(1, tracerProvider.shutdownCount)
        assertEquals(1, loggerProvider.shutdownCount)
    }

    @Test
    fun testForceFlushFailure() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(flushResult = Failure)
        val loggerProvider = FakeCloseableLoggerProvider(flushResult = Failure)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.forceFlush()
        assertEquals(Failure, result)
        assertEquals(1, tracerProvider.flushCount)
        assertEquals(1, loggerProvider.flushCount)
    }

    @Test
    fun testShutdownFailure() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(shutdownResult = Failure)
        val loggerProvider = FakeCloseableLoggerProvider(shutdownResult = Failure)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.shutdown()
        assertEquals(Failure, result)
        assertEquals(1, tracerProvider.shutdownCount)
        assertEquals(1, loggerProvider.shutdownCount)
    }

    @Test
    fun testForceFlushCombinedResult() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(flushResult = Success)
        val loggerProvider = FakeCloseableLoggerProvider(flushResult = Failure)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.forceFlush()
        assertEquals(Failure, result)
        assertEquals(1, tracerProvider.flushCount)
        assertEquals(1, loggerProvider.flushCount)
    }

    @Test
    fun testShutdownCombinedResult() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(shutdownResult = Failure)
        val loggerProvider = FakeCloseableLoggerProvider(shutdownResult = Success)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.shutdown()
        assertEquals(Failure, result)
        assertEquals(1, tracerProvider.shutdownCount)
        assertEquals(1, loggerProvider.shutdownCount)
    }

    @Test
    fun testForceFlushSmallTimeout() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(flushDelayMs = 10)
        val loggerProvider = FakeCloseableLoggerProvider(flushDelayMs = 10)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.forceFlush()
        assertEquals(Success, result)
        assertEquals(1, tracerProvider.flushCount)
        assertEquals(1, loggerProvider.flushCount)
    }

    @Test
    fun testShutdownSmallTimeout() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(shutdownDelayMs = 10)
        val loggerProvider = FakeCloseableLoggerProvider(shutdownDelayMs = 10)
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.shutdown()
        assertEquals(Success, result)
        assertEquals(1, tracerProvider.shutdownCount)
        assertEquals(1, loggerProvider.shutdownCount)
    }

    @Test
    fun testForceFlushExceedsTimeout() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(flushDelayMs = 5000)
        val loggerProvider = FakeCloseableLoggerProvider()
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.forceFlush()
        assertEquals(Failure, result)
    }

    @Test
    fun testShutdownExceedsTimeout() = runTest {
        val tracerProvider = FakeCloseableTracerProvider(shutdownDelayMs = 5000)
        val loggerProvider = FakeCloseableLoggerProvider()
        val api = createOpenTelemetry(tracerProvider, loggerProvider)

        val result = api.shutdown()
        assertEquals(Failure, result)
    }

    // TODO: test cases where telemetry submitted after shutdown. For tracer/logger impl in separate PR?

    private fun createOpenTelemetry(
        tracerProvider: TracerProvider,
        loggerProvider: LoggerProvider
    ): TelemetryCloseable = CloseableOpenTelemetryImpl(
        tracerProvider = tracerProvider,
        loggerProvider = loggerProvider,
        clock = FakeClock(),
        sdkFactory = FakeSdkFactory(),
    )

    private class FakeCloseableTracerProvider(
        private val flushResult: OperationResultCode = Success,
        private val shutdownResult: OperationResultCode = Success,
        private val flushException: Throwable? = null,
        private val shutdownException: Throwable? = null,
        private val flushDelayMs: Long = 0,
        private val shutdownDelayMs: Long = 0,
        private val delegate: FakeTracerProvider = FakeTracerProvider(),
    ) : TracerProvider by delegate, TelemetryCloseable {

        var flushCount = 0
        var shutdownCount = 0

        override suspend fun forceFlush(): OperationResultCode {
            flushCount++
            delay(flushDelayMs)
            flushException?.let { throw it }
            return flushResult
        }

        override suspend fun shutdown(): OperationResultCode {
            shutdownCount++
            delay(shutdownDelayMs)
            shutdownException?.let { throw it }
            return shutdownResult
        }
    }

    private class FakeCloseableLoggerProvider(
        private val flushResult: OperationResultCode = Success,
        private val shutdownResult: OperationResultCode = Success,
        private val flushException: Throwable? = null,
        private val shutdownException: Throwable? = null,
        private val flushDelayMs: Long = 0,
        private val shutdownDelayMs: Long = 0,
        private val delegate: FakeLoggerProvider = FakeLoggerProvider(),
    ) : LoggerProvider by delegate, TelemetryCloseable {

        var flushCount = 0
        var shutdownCount = 0

        override suspend fun forceFlush(): OperationResultCode {
            flushCount++
            delay(flushDelayMs)
            flushException?.let { throw it }
            return flushResult
        }

        override suspend fun shutdown(): OperationResultCode {
            shutdownCount++
            delay(shutdownDelayMs)
            shutdownException?.let { throw it }
            return shutdownResult
        }
    }
}
