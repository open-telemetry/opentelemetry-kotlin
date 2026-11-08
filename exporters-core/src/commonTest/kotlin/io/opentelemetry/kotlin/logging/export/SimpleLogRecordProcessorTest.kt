package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
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

    @Test
    fun testSpanProcessorDefaults() = runTest {
        val processor = FakeLogExportConfig().simpleLogRecordProcessor(FakeLogRecordExporter())
        assertTrue(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testSpanProcessorExport() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )

        val log = FakeReadWriteLogRecord(body = "my_log")
        processor.onEmit(log, FakeContext())

        val export = exporter.logs.single()
        assertEquals(log.body, export.body)
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

        val log = FakeReadWriteLogRecord()
        processor.onEmit(log, FakeContext())
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
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(StandardTestDispatcher(testScheduler)),
            NoopSdkErrorHandler,
        )
        repeat(5) { processor.onEmit(FakeReadWriteLogRecord(body = "log_$it"), FakeContext()) }
        advanceUntilIdle()

        assertFalse(exporter.observedConcurrentExport)
        assertEquals(List(5) { "log_$it" }, exporter.exports)
    }

    @Test
    fun testForceFlushWaitsForPendingExports() = runTest {
        val exporter = SlowLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())

        // Force flush should wait for exports to complete
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        assertEquals(2, exporter.exports.size)
    }

    @Test
    fun testShutdownWaitsForPendingExports() = runTest {
        val exporter = SlowLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())

        // Shutdown should wait for exports to complete
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(2, exporter.exports.size)
    }

    @Test
    fun testConcurrentForceFlush() = runTest {
        val exporter = SlowLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())

        // Multiple concurrent forceFlush calls should all succeed
        val flushResults = listOf(
            async { processor.forceFlush() },
            async { processor.forceFlush() },
            async { processor.forceFlush() }
        ).awaitAll()

        assertTrue(flushResults.all { it == OperationResultCode.Success })
    }

    @Test
    fun testExportFailureDoesNotCrashProcessor() = runTest {
        val exporter = FailingLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        // Processor should still work after failure
        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        advanceUntilIdle()

        assertEquals(2, exporter.exportCallCount)
    }

    @Test
    fun testForceFlushWaitsForExportAlreadyExecuting() = runTest {
        val exporter = BlockingLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        val flushResult = async { processor.forceFlush() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        assertEquals(listOf("log1"), exporter.exports)
    }

    @Test
    fun testForceFlushWaitsForExportQueuedBehindMutex() = runTest {
        val exporter = BlockingLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        runCurrent()

        val flushResult = async { processor.forceFlush() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        assertEquals(listOf("log1", "log2"), exporter.exports)
    }

    @Test
    fun testForceFlushCallsExporterFlushOnlyAfterExportsFinish() = runTest {
        val exporter = OrderTrackingLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        val flushResult = async { processor.forceFlush() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        assertEquals(listOf("export:log1", "forceFlush"), exporter.events)
    }

    @Test
    fun testShutdownWaitsForExportAlreadyExecuting() = runTest {
        val exporter = BlockingLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        val shutdownResult = async { processor.shutdown() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, shutdownResult.await())
        assertEquals(listOf("log1"), exporter.exports)
    }

    @Test
    fun testShutdownWaitsForExportsQueuedBehindMutex() = runTest {
        val exporter = BlockingLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        runCurrent()

        val shutdownResult = async { processor.shutdown() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, shutdownResult.await())
        assertEquals(listOf("log1", "log2"), exporter.exports)
    }

    @Test
    fun testShutdownCallsExporterFlushAndShutdownAfterDrain() = runTest {
        val exporter = OrderTrackingLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        val shutdownResult = async { processor.shutdown() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, shutdownResult.await())
        assertEquals(listOf("export:log1", "forceFlush", "shutdown"), exporter.events)
    }

    @Test
    fun testExportTimeoutReleasesMutexAndDoesNotBlockProcessor() = runTest {
        val exporter = ConfigurableDelayLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        exporter.delayMs = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS + 1000
        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        assertTrue(exporter.exports.isEmpty())

        exporter.delayMs = 0
        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        advanceUntilIdle()

        assertEquals(listOf("log2"), exporter.exports)
    }

    @Test
    fun testExporterReturningFailureDoesNotBlockProcessor() = runTest {
        val exporter = FailureResultLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        assertEquals(1, exporter.exportCount)

        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        advanceUntilIdle()

        assertEquals(2, exporter.exportCount)
    }

    @Test
    fun testConcurrentSubmissionAndFlushDoNotDeadlock() = runTest {
        val exporter = SlowLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        repeat(10) { processor.onEmit(FakeReadWriteLogRecord(body = "log_$it"), FakeContext()) }

        val flushResult = async { processor.forceFlush() }
        repeat(10) { processor.onEmit(FakeReadWriteLogRecord(body = "extra_$it"), FakeContext()) }
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        assertTrue(exporter.exports.size >= 10)
    }

    // Regression tests for Copilot review findings

    @Test
    fun testForceFlushPropagatesExportFailure() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FailureResultLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, errorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        processor.forceFlush()

        // Export failure should be reported to error handler
        assertTrue(errorHandler.sdkCodeErrors.isNotEmpty())
        val error = errorHandler.sdkCodeErrors.first()
        assertTrue(error.message.contains("Export failed"))
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun testForceFlushPropagatesExportException() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FailingLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, errorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        processor.forceFlush()

        // Export exception should be reported by guardExporterCode
        assertTrue(errorHandler.sdkCodeErrors.isNotEmpty())
    }

    @Test
    fun testForceFlushHandlesMixedExportOutcomes() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = OutcomeVaryingLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, errorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        processor.onEmit(FakeReadWriteLogRecord(body = "log2"), FakeContext())
        processor.onEmit(FakeReadWriteLogRecord(body = "log3"), FakeContext())
        advanceUntilIdle()

        processor.forceFlush()

        // Failures should be reported, successes should not
        assertTrue(errorHandler.sdkCodeErrors.size >= 2)
    }

    @Test
    fun testShutdownNoRaceWithExportAdmission() = runTest {
        val exporter = SlowLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        // Start an export
        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())

        // Shutdown immediately - should either admit the export or reject it, not race
        val shutdownResult = processor.shutdown()

        assertEquals(OperationResultCode.Success, shutdownResult)
        // Either 0 or 1 exports depending on whether shutdown beat the admission
        assertTrue(exporter.exports.size <= 1)
    }

    @Test
    fun testShutdownCallsExporterCleanup() = runTest {
        val exporter = SlowLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())

        // Shutdown should wait for export and call exporter cleanup
        val shutdownResult = processor.shutdown()

        assertEquals(OperationResultCode.Success, shutdownResult)
        assertTrue(exporter.shutdownCalled)
    }

    @Test
    fun testShutdownCallsExporterFlushBeforeShutdown() = runTest {
        val exporter = OrderTrackingLogRecordExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        runCurrent()

        val shutdownResult = async { processor.shutdown() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, shutdownResult.await())
        assertEquals(listOf("export:log1", "forceFlush", "shutdown"), exporter.events)
    }

    @Test
    fun testExporterReturningFailureReportedToErrorHandler() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FailureResultLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, errorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        // Call forceFlush to trigger failure reporting
        processor.forceFlush()

        // Failure result should be reported
        assertTrue(errorHandler.sdkCodeErrors.isNotEmpty())
        val error = errorHandler.sdkCodeErrors.first()
        assertTrue(error.message.contains("Export failed"))
    }

    @Test
    fun testExporterExceptionReportedOnce() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FailingLogRecordExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleLogRecordProcessor(exporter, scope, errorHandler)

        processor.onEmit(FakeReadWriteLogRecord(body = "log1"), FakeContext())
        advanceUntilIdle()

        processor.forceFlush()

        // Exception should be reported exactly once (by guardExporterCode)
        assertEquals(1, errorHandler.sdkCodeErrors.size)
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
            exports.addAll(telemetry.map { it.body.toString() })
            activeExports--
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success

        private companion object {
            const val EXPORT_DURATION_MS = 2L
        }
    }

    private class SlowLogRecordExporter : LogRecordExporter {
        val exports: MutableList<String> = mutableListOf()
        var shutdownCalled = false

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            delay(10)
            exports.addAll(telemetry.map { it.body.toString() })
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode {
            shutdownCalled = true
            return OperationResultCode.Success
        }
    }

    private class FailingLogRecordExporter : LogRecordExporter {
        var exportCallCount = 0

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            exportCallCount++
            error("Export failed")
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    /** Blocks the first export until [release] is completed, then records the log body. */
    private class BlockingLogRecordExporter : LogRecordExporter {
        val exports = mutableListOf<String>()
        private val release = CompletableDeferred<Unit>()

        fun release() = release.complete(Unit)

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            release.await()
            exports += telemetry.map { it.body.toString() }
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    /** Records the order of export, forceFlush, and shutdown calls. */
    private class OrderTrackingLogRecordExporter : LogRecordExporter {
        val events = mutableListOf<String>()
        private val release = CompletableDeferred<Unit>()

        fun release() = release.complete(Unit)

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            events += "export:${telemetry.first().body}"
            release.await()
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

    /** Exporter whose delay is configurable per export. */
    private class ConfigurableDelayLogRecordExporter : LogRecordExporter {
        val exports = mutableListOf<String>()
        var delayMs: Long = 0

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            if (delayMs > 0) {
                delay(delayMs)
            }
            exports += telemetry.map { it.body.toString() }
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    /** Always returns [OperationResultCode.Failure] from export. */
    private class FailureResultLogRecordExporter : LogRecordExporter {
        var exportCount = 0

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            exportCount++
            return OperationResultCode.Failure
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    /** Varies export outcome between success, failure, and exception. */
    private class OutcomeVaryingLogRecordExporter : LogRecordExporter {
        var callCount = 0

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            callCount++
            return when (callCount % 3) {
                0 -> OperationResultCode.Success
                1 -> OperationResultCode.Failure
                else -> error("Simulated export failure")
            }
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }
}
