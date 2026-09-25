package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
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
internal class BatchSpanProcessorImplTest {

    private lateinit var exporter: FakeSpanExporter
    private lateinit var processor: BatchSpanProcessorImpl
    private val dispatcher = StandardTestDispatcher()
    private val events = mutableListOf<String>()
    private var shutdownResult: OperationResultCode = OperationResultCode.Success
    private var shutdownError: Throwable? = null
    private val errorHandler = FakeSdkErrorHandler()

    @BeforeTest
    fun setup() {
        exporter = FakeSpanExporter(
            exportReturnValue = { record("export ${it.size}") },
            forceFlushReturnValue = { record("flush") },
            shutdownReturnValue = {
                shutdownError?.let { throw it }
                record("shutdown", shutdownResult)
            },
        )
        processor = BatchSpanProcessorImpl(
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
    fun testOnlyOnEndIsRequired() = runTest(dispatcher) {
        assertFalse(processor.isStartRequired())
        assertTrue(processor.isEndRequired())
        assertFalse(processor.isOnEndingRequired())
        processor.shutdown()
        advanceUntilIdle()
    }

    @Test
    fun testOnEndNoOpAfterShutdown() = runTest(dispatcher) {
        processor.shutdown()
        advanceUntilIdle()

        val span = FakeReadWriteSpan()
        processor.onEnd(span)
        advanceUntilIdle()

        assertTrue(exporter.exports.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest(dispatcher) {
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest(dispatcher) {
        processor.shutdown()
        advanceUntilIdle()
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testOnEndSkipsUnsampledSpan() = runTest(dispatcher) {
        val span = FakeReadWriteSpan(
            spanContext = FakeSpanContext(traceFlags = FakeTraceFlags(isSampled = false))
        )

        processor.onEnd(span)
        processor.forceFlush()
        processor.shutdown()
        advanceUntilIdle()

        assertTrue(exporter.exports.isEmpty())
    }

    @Test
    fun testForceFlushAndShutdownExportBeforeDelegating() = runTest(dispatcher) {
        shutdownResult = OperationResultCode.Failure
        processor.onEnd(FakeReadWriteSpan())
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        processor.onEnd(FakeReadWriteSpan())
        assertEquals(OperationResultCode.Failure, processor.shutdown())
        assertEquals(listOf("export 1", "flush", "export 1", "shutdown"), events)
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
