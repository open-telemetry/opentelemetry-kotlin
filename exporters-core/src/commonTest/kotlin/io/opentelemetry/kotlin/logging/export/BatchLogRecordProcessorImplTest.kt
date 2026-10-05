package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class BatchLogRecordProcessorImplTest {

    private lateinit var exporter: FakeLogRecordExporter
    private lateinit var processor: BatchLogRecordProcessorImpl
    private val dispatcher = StandardTestDispatcher()
    private val events = mutableListOf<String>()
    private var shutdownResult: OperationResultCode = OperationResultCode.Success
    private var shutdownError: Throwable? = null
    private val errorHandler = FakeSdkErrorHandler()

    @BeforeTest
    fun setup() {
        exporter = FakeLogRecordExporter(
            flushCode = { record("flush") },
            shutdownCode = {
                shutdownError?.let { throw it }
                record("shutdown", shutdownResult)
            },
            action = { record("export ${it.size}") },
        )
        processor = BatchLogRecordProcessorImpl(
            exporter = exporter,
            maxQueueSize = 100,
            scheduleDelayMs = 1,
            exportTimeoutMs = 1000,
            maxExportBatchSize = 10,
            sdkErrorHandler = errorHandler,
            dispatcher = dispatcher,
        )
    }

    @Test
    fun testExportReceivesImmutableSnapshot() = runTest(dispatcher) {
        processor.shutdown()

        val testProcessor = BatchLogRecordProcessorImpl(
            exporter = exporter,
            maxQueueSize = 100,
            scheduleDelayMs = 1,
            exportTimeoutMs = 1000,
            maxExportBatchSize = 10,
            sdkErrorHandler = NoopSdkErrorHandler,
            dispatcher = StandardTestDispatcher(testScheduler),
        )

        val log = FakeReadWriteLogRecord(body = "my_log")
        testProcessor.onEmit(log, FakeContext())
        log.body = "changed"
        assertEquals(OperationResultCode.Success, testProcessor.forceFlush())

        // the queued batch retains plain data rather than the live log record
        val export = exporter.logs.single()
        assertFalse(export === log)
        assertEquals("my_log", export.body)

        testProcessor.shutdown()
    }

    @Test
    fun testOnEmitNoOpAfterShutdown() = runTest(dispatcher) {
        processor.shutdown()
        advanceUntilIdle()

        val log = FakeReadWriteLogRecord()
        processor.onEmit(log, FakeContext())
        advanceUntilIdle()

        assertTrue(exporter.logs.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest(dispatcher) {
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testForceFlushAndShutdownExportBeforeDelegating() = runTest(dispatcher) {
        shutdownResult = OperationResultCode.Failure
        processor.onEmit(FakeReadWriteLogRecord(), FakeContext())
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        processor.onEmit(FakeReadWriteLogRecord(), FakeContext())
        assertEquals(OperationResultCode.Failure, processor.shutdown())
        assertEquals(listOf("export 1", "flush", "export 1", "shutdown"), events)
    }

    @Test
    fun testEnabledReturnsFalseAfterShutdown() = runTest(dispatcher) {
        assertTrue(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
        processor.shutdown()
        assertFalse(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest(dispatcher) {
        processor.shutdown()
        advanceUntilIdle()
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testExporterShutdownThrowingIsReported() = runTest(dispatcher) {
        shutdownError = IllegalStateException("boom")
        assertEquals(OperationResultCode.Failure, processor.shutdown())
        assertEquals(1, errorHandler.userCodeErrors.size)
    }

    @Test
    fun testExporterShutdownCancellationDoesNotEscape() = runTest(dispatcher) {
        shutdownError = CancellationException("exporter cancelled")
        assertEquals(OperationResultCode.Failure, processor.shutdown())
        assertEquals(1, errorHandler.userCodeErrors.size)
    }

    private fun record(event: String, result: OperationResultCode = OperationResultCode.Success): OperationResultCode {
        events += event
        return result
    }
}
