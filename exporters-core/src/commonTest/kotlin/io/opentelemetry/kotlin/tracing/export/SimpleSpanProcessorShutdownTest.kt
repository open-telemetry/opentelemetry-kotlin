package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Shutdown lifecycle and export error-reporting tests for [SimpleSpanProcessor].
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class SimpleSpanProcessorShutdownTest {

    @Test
    fun testShutdownDrainTimeoutStillRunsExporterCleanup() = runTest {
        val exporter = GatedSpanExporter(exportGate = CompletableDeferred())
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent() // export starts and blocks on the gate

        val result = async { processor.shutdown() }
        advanceUntilIdle() // drain hits SHUTDOWN_TIMEOUT_MS, jobs cancelled, cleanup runs

        assertEquals(OperationResultCode.Failure, result.await())
        assertEquals(1, exporter.shutdownCalls)
        assertEquals(listOf("forceFlush", "shutdown"), exporter.events)
    }

    @Test
    fun testShutdownForceFlushTimeoutDoesNotSkipExporterShutdown() = runTest {
        val exporter = GatedSpanExporter(flushGate = CompletableDeferred())
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent()

        val result = async { processor.shutdown() }
        advanceUntilIdle() // drain succeeds; exporter.forceFlush times out, then shutdown runs

        assertEquals(OperationResultCode.Failure, result.await())
        assertEquals(1, exporter.shutdownCalls)
        assertEquals(listOf("export:span1", "forceFlush", "shutdown"), exporter.events)
    }

    @Test
    fun testShutdownNormalPathOrdering() = runTest {
        val exporter = GatedSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent()

        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(1, exporter.shutdownCalls)
        assertEquals(listOf("export:span1", "forceFlush", "shutdown"), exporter.events)
    }

    @Test
    fun testShutdownAfterDrainTimeoutDoesNotRepeatCleanup() = runTest {
        val exporter = GatedSpanExporter(exportGate = CompletableDeferred())
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent()

        assertEquals(OperationResultCode.Failure, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(1, exporter.shutdownCalls)
    }

    @Test
    fun testExportFailureResultIsReportedToErrorHandler() = runTest {
        val handler = FakeSdkErrorHandler()
        val exporter = StaticResultSpanExporter(OperationResultCode.Failure)
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, handler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle()

        assertEquals(1, handler.userCodeErrors.size)
        assertEquals("Export failed", handler.userCodeErrors.single().message)

        // Processor remains operational after a failed export.
        processor.onEnd(FakeReadWriteSpan(name = "span2"))
        advanceUntilIdle()
        assertEquals(2, exporter.exportCount)
        assertEquals(2, handler.userCodeErrors.size)
    }

    @Test
    fun testExportTimeoutIsReportedToErrorHandler() = runTest {
        val handler = FakeSdkErrorHandler()
        val exporter = DelayedSpanExporter(BatchTelemetryDefaults.EXPORT_TIMEOUT_MS + 1000)
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, handler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle() // export times out at EXPORT_TIMEOUT_MS

        assertEquals(1, handler.userCodeErrors.size)
        assertEquals("Export timed out", handler.userCodeErrors.single().message)
        assertTrue(exporter.exports.isEmpty())
    }

    @Test
    fun testSuccessfulExportIsNotReported() = runTest {
        val handler = FakeSdkErrorHandler()
        val exporter = StaticResultSpanExporter(OperationResultCode.Success)
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, handler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle()

        assertTrue(handler.errors.isEmpty())
    }

    @Test
    fun testExportExceptionIsReportedExactlyOnce() = runTest {
        val handler = FakeSdkErrorHandler()
        val exporter = ThrowingSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, handler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle()

        assertEquals(1, handler.userCodeErrors.size)
        assertEquals("Export failed", handler.userCodeErrors.single().message)
    }

    /**
     * Exporter whose export and forceFlush can be gated; lifecycle events are recorded
     * when the corresponding call completes (export) or is invoked (flush/shutdown).
     */
    private class GatedSpanExporter(
        private val exportGate: CompletableDeferred<Unit>? = null,
        private val flushGate: CompletableDeferred<Unit>? = null,
    ) : SpanExporter {
        val events = mutableListOf<String>()
        var shutdownCalls = 0

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            exportGate?.await()
            events += "export:${telemetry.first().name}"
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode {
            events += "forceFlush"
            flushGate?.await()
            return OperationResultCode.Success
        }

        override suspend fun shutdown(): OperationResultCode {
            events += "shutdown"
            shutdownCalls++
            return OperationResultCode.Success
        }
    }

    private class StaticResultSpanExporter(private val result: OperationResultCode) : SpanExporter {
        var exportCount = 0

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            exportCount++
            return result
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    private class DelayedSpanExporter(private val delayMs: Long) : SpanExporter {
        val exports = mutableListOf<String>()

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            delay(delayMs)
            exports += telemetry.map(SpanData::name)
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    private class ThrowingSpanExporter : SpanExporter {
        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            error("Export failed")
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }
}
