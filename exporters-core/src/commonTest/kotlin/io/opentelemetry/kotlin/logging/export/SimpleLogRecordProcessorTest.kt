package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class SimpleLogRecordProcessorTest {

    private fun newProcessor(
        exporter: LogRecordExporter,
        handler: SdkErrorHandler = NoopSdkErrorHandler,
        testScheduler: TestCoroutineScheduler,
    ) = SimpleLogRecordProcessor(exporter, CoroutineScope(StandardTestDispatcher(testScheduler)), handler)

    @Test
    fun testLogRecordProcessorDefaults() = runTest {
        val processor = FakeLogExportConfig().simpleLogRecordProcessor(FakeLogRecordExporter())
        assertTrue(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testLogRecordProcessorExport() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )

        val log = FakeReadWriteLogRecord(body = "my_log")
        processor.onEmit(log, FakeContext())

        assertEquals(log.body, exporter.logs.single().body)
    }

    @Test
    fun testExportReceivesImmutableSnapshot() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )

        val log = FakeReadWriteLogRecord(body = "my_log")
        processor.onEmit(log, FakeContext())

        // the exporter retains plain data rather than the live log record
        val export = exporter.logs.single()
        assertFalse(export === log)
        log.body = "changed"
        assertEquals("my_log", export.body)
    }

    @Test
    fun testOnEmitNoOpAfterShutdown() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )
        processor.shutdown()

        processor.onEmit(FakeReadWriteLogRecord(), FakeContext())
        assertTrue(exporter.logs.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )

        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testEnabledReturnsFalseAfterShutdown() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )
        processor.shutdown()

        assertFalse(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )
        processor.shutdown()

        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testConcurrentExportsAreSerialized() = runTest {
        val exporter = SuspendingLogRecordExporter()
        val processor = newProcessor(exporter, testScheduler = testScheduler)

        repeat(5) { processor.onEmit(FakeReadWriteLogRecord(body = "log_$it"), FakeContext()) }
        advanceUntilIdle()

        assertFalse(exporter.observedConcurrentExport)
        assertEquals(List(5) { "log_$it" }, exporter.exports)
    }

    @Test
    fun testConcurrentFlushAndSubmissionDoNotDeadlock() = runTest {
        val exporter = OrderTrackingLogRecordExporter()
        exporter.delayMs = 10
        val processor = newProcessor(exporter, testScheduler = testScheduler)

        repeat(10) { processor.onEmit(FakeReadWriteLogRecord(body = "log_$it"), FakeContext()) }

        // Multiple concurrent flushes while more logs are submitted must all succeed.
        val flushResults = listOf(
            async { processor.forceFlush() },
            async { processor.forceFlush() },
            async { processor.forceFlush() },
        )
        repeat(10) { processor.onEmit(FakeReadWriteLogRecord(body = "extra_$it"), FakeContext()) }
        advanceUntilIdle()

        assertTrue(flushResults.awaitAll().all { it == OperationResultCode.Success })
        assertTrue(exporter.exports.size >= 10)
    }

    @Test
    fun testFlushAndShutdownWaitForExportsBeforeExporterCalls() = runTest {
        // One blocked export plus one queued export must finish before exporter calls.
        suspend fun drainScenario(shutdown: Boolean): Pair<OrderTrackingLogRecordExporter, OperationResultCode> {
            val exporter = OrderTrackingLogRecordExporter(gated = true)
            val processor = newProcessor(exporter, testScheduler = testScheduler)
            processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
            runCurrent()
            processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
            runCurrent()
            val result = async { if (shutdown) { processor.shutdown() } else { processor.forceFlush() } }
            runCurrent()
            exporter.release()
            advanceUntilIdle()
            return exporter to result.await()
        }

        val (flushExporter, flushResult) = drainScenario(shutdown = false)
        assertEquals(OperationResultCode.Success, flushResult)
        assertEquals(listOf("export:log1", "export:log2", "forceFlush"), flushExporter.events)

        val (shutdownExporter, shutdownResult) = drainScenario(shutdown = true)
        assertEquals(OperationResultCode.Success, shutdownResult)
        assertEquals(
            listOf("export:log1", "export:log2", "forceFlush", "shutdown"),
            shutdownExporter.events,
        )
    }

    @Test
    fun testExportTimeoutReleasesMutexAndDoesNotBlockProcessor() = runTest {
        val exporter = OrderTrackingLogRecordExporter()
        val processor = newProcessor(exporter, testScheduler = testScheduler)

        exporter.delayMs = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS + 1000
        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle() // export times out

        assertTrue(exporter.exports.isEmpty()) // log1 was not exported (timed out)

        // A subsequent export should succeed — the mutex must have been released
        exporter.delayMs = 0
        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        advanceUntilIdle()

        assertEquals(listOf("log2"), exporter.exports)
    }

    @Test
    fun testShutdownCleanupAndExportErrorReporting() = runTest {
        // A gated export makes the shutdown drain hit SHUTDOWN_TIMEOUT_MS; cleanup must
        // still run exactly once and a repeated shutdown must not repeat it.
        val drainExporter = TestLogRecordExporter(exportGate = CompletableDeferred())
        val drainProcessor = newProcessor(drainExporter, testScheduler = testScheduler)
        drainProcessor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        val drainResult = async { drainProcessor.shutdown() }
        advanceUntilIdle()

        assertEquals(OperationResultCode.Failure, drainResult.await())
        assertEquals(1, drainExporter.shutdownCalls)
        assertEquals(listOf("forceFlush", "shutdown"), drainExporter.events)

        assertEquals(OperationResultCode.Success, drainProcessor.shutdown())
        assertEquals(1, drainExporter.shutdownCalls)

        // A gated exporter forceFlush makes cleanup's flush phase time out; shutdown
        // still completes all exporter calls in order.
        val flushExporter = TestLogRecordExporter(flushGate = CompletableDeferred())
        val flushProcessor = newProcessor(flushExporter, testScheduler = testScheduler)
        flushProcessor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        val flushResult = async { flushProcessor.shutdown() }
        advanceUntilIdle()

        assertEquals(OperationResultCode.Failure, flushResult.await())
        assertEquals(1, flushExporter.shutdownCalls)
        assertEquals(listOf("export:log1", "forceFlush", "shutdown"), flushExporter.events)

        // An unsuccessful export result is reported once; a later success is not.
        val handler = FakeSdkErrorHandler()
        var result: OperationResultCode = OperationResultCode.Failure
        var exportCount = 0
        val resultProcessor = newProcessor(
            TestLogRecordExporter {
                exportCount++
                result
            },
            handler,
            testScheduler,
        )
        resultProcessor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()
        assertEquals(1, handler.userCodeErrors.size)
        assertEquals("Export failed", handler.userCodeErrors.single().message)

        result = OperationResultCode.Success
        resultProcessor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        advanceUntilIdle()
        assertEquals(1, handler.userCodeErrors.size)
        assertEquals(2, exportCount)

        // An export exceeding EXPORT_TIMEOUT_MS is reported as a timeout.
        val timeoutHandler = FakeSdkErrorHandler()
        val timeoutProcessor = newProcessor(
            TestLogRecordExporter(exportGate = CompletableDeferred()),
            timeoutHandler,
            testScheduler,
        )
        timeoutProcessor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()
        assertEquals(1, timeoutHandler.userCodeErrors.size)
        assertEquals("Export timed out", timeoutHandler.userCodeErrors.single().message)

        // Each thrown exporter exception is reported per export.
        val throwHandler = FakeSdkErrorHandler()
        val throwProcessor = newProcessor(
            TestLogRecordExporter { error("Export failed") },
            throwHandler,
            testScheduler,
        )
        throwProcessor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        throwProcessor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        advanceUntilIdle()
        assertEquals(2, throwHandler.userCodeErrors.size)
        assertTrue(throwHandler.userCodeErrors.all { it.message == "Export failed" })
    }

    @Test
    fun testCancelledExportIsNotReportedAsFailure() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = OrderTrackingLogRecordExporter(gated = true)
        val scopeJob = Job()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(scopeJob + StandardTestDispatcher(testScheduler)),
            errorHandler,
        )

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        // Cancelling the scope cancels the in-flight export; cancellation must
        // propagate rather than being reported as an export failure.
        scopeJob.cancel()
        advanceUntilIdle()

        processor.forceFlush()

        assertTrue(errorHandler.sdkCodeErrors.isEmpty())
        assertTrue(errorHandler.userCodeErrors.isEmpty())
    }

    @Test
    fun testEagerlyCompletedExportLeavesNoStaleTrackingEntry() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            errorHandler,
        )

        // UnconfinedTestDispatcher runs the export coroutine eagerly, so it can
        // complete before onEmit finishes tracking it.
        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        // A successful export must never be reported, no matter how often we drain
        processor.forceFlush()
        processor.forceFlush()
        processor.shutdown()

        assertTrue(errorHandler.sdkCodeErrors.isEmpty())
        assertTrue(errorHandler.userCodeErrors.isEmpty())
    }

    private class SuspendingLogRecordExporter : LogRecordExporter {
        val exports: MutableList<String> = mutableListOf()
        var observedConcurrentExport: Boolean = false
        private var activeExports: Int = 0

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            activeExports++
            if (activeExports > 1) {
                observedConcurrentExport = true
            }
            delay(EXPORT_DURATION_MS)
            exports += telemetry.map { it.body.toString() }
            activeExports--
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success

        private companion object {
            const val EXPORT_DURATION_MS = 2L
        }
    }

    /** Exporter recording export completions and lifecycle calls in [events]. */
    private class OrderTrackingLogRecordExporter(private val gated: Boolean = false) : LogRecordExporter {
        val events = mutableListOf<String>()
        val exports: List<String>
            get() = events.filter { it.startsWith("export:") }.map { it.removePrefix("export:") }
        private val release = CompletableDeferred<Unit>()
        var delayMs: Long = 0

        fun release() = release.complete(Unit)

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            if (gated) {
                release.await()
            }
            delay(delayMs)
            events += "export:${telemetry.first().body}"
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode {
            events += "forceFlush"
            return OperationResultCode.Success
        }

        override suspend fun shutdown(): OperationResultCode {
            events += "shutdown"
            return OperationResultCode.Success
        }
    }

    private class TestLogRecordExporter(
        private val exportGate: CompletableDeferred<Unit>? = null,
        private val flushGate: CompletableDeferred<Unit>? = null,
        private val export: (suspend () -> OperationResultCode)? = null,
    ) : LogRecordExporter {
        val events = mutableListOf<String>()
        var shutdownCalls = 0

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            exportGate?.await()
            events += "export:${telemetry.first().body}"
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
