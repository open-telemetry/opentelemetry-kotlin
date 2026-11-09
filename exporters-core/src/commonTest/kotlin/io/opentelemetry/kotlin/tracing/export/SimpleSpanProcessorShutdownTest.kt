package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Shutdown lifecycle and export error-reporting tests for [SimpleSpanProcessor]. */
@OptIn(ExperimentalCoroutinesApi::class)
internal class SimpleSpanProcessorShutdownTest {

    private fun newProcessor(
        exporter: SpanExporter,
        handler: SdkErrorHandler = NoopSdkErrorHandler,
        testScheduler: TestCoroutineScheduler,
    ) = SimpleSpanProcessor(exporter, CoroutineScope(StandardTestDispatcher(testScheduler)), handler)

    @Test
    fun testShutdownCleanupAndExportErrorReporting() = runTest {
        val drainExporter = TestSpanExporter(exportGate = CompletableDeferred())
        val drainProcessor = newProcessor(drainExporter, testScheduler = testScheduler)
        drainProcessor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent()

        val drainResult = async { drainProcessor.shutdown() }
        advanceUntilIdle() // drain hits SHUTDOWN_TIMEOUT_MS, jobs cancelled, cleanup runs

        assertEquals(OperationResultCode.Failure, drainResult.await())
        assertEquals(1, drainExporter.shutdownCalls)
        assertEquals(listOf("forceFlush", "shutdown"), drainExporter.events)

        assertEquals(OperationResultCode.Success, drainProcessor.shutdown())
        assertEquals(1, drainExporter.shutdownCalls)

        val flushExporter = TestSpanExporter(flushGate = CompletableDeferred())
        val flushProcessor = newProcessor(flushExporter, testScheduler = testScheduler)
        flushProcessor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent()

        val flushResult = async { flushProcessor.shutdown() }
        advanceUntilIdle()

        assertEquals(OperationResultCode.Failure, flushResult.await())
        assertEquals(1, flushExporter.shutdownCalls)
        assertEquals(listOf("export:span1", "forceFlush", "shutdown"), flushExporter.events)
        val handler = FakeSdkErrorHandler()
        var result: OperationResultCode = OperationResultCode.Failure
        var exportCount = 0
        val resultProcessor = newProcessor(
            TestSpanExporter {
                exportCount++
                result
            },
            handler,
            testScheduler,
        )
        resultProcessor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle()
        assertEquals(1, handler.userCodeErrors.size)
        assertEquals("Export failed", handler.userCodeErrors.single().message)

        result = OperationResultCode.Success
        resultProcessor.onEnd(FakeReadWriteSpan(name = "span2"))
        advanceUntilIdle()
        assertEquals(1, handler.userCodeErrors.size)
        assertEquals(2, exportCount)
        val timeoutHandler = FakeSdkErrorHandler()
        val timeoutProcessor = newProcessor(
            TestSpanExporter(exportGate = CompletableDeferred()),
            timeoutHandler,
            testScheduler,
        )
        timeoutProcessor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle()
        assertEquals(1, timeoutHandler.userCodeErrors.size)
        assertEquals("Export timed out", timeoutHandler.userCodeErrors.single().message)
        val throwHandler = FakeSdkErrorHandler()
        val throwProcessor = newProcessor(
            TestSpanExporter { error("Export failed") },
            throwHandler,
            testScheduler,
        )
        throwProcessor.onEnd(FakeReadWriteSpan(name = "span1"))
        throwProcessor.onEnd(FakeReadWriteSpan(name = "span2"))
        advanceUntilIdle()
        assertEquals(2, throwHandler.userCodeErrors.size)
        assertTrue(throwHandler.userCodeErrors.all { it.message == "Export failed" })
    }

    private class TestSpanExporter(
        private val exportGate: CompletableDeferred<Unit>? = null,
        private val flushGate: CompletableDeferred<Unit>? = null,
        private val export: (suspend () -> OperationResultCode)? = null,
    ) : SpanExporter {
        val events = mutableListOf<String>()
        var shutdownCalls = 0

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            exportGate?.await()
            events += "export:${telemetry.first().name}"
            return export?.invoke() ?: OperationResultCode.Success
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
}
