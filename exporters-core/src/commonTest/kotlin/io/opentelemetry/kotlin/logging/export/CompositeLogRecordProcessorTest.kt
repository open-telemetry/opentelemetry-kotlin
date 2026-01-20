package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompositeLogRecordProcessorTest {

    private val fakeLogRecord = FakeReadWriteLogRecord()
    private val fakeContext = FakeContext()
    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        errorHandler = FakeSdkErrorHandler()
    }

    @Test
    fun testNoSpanProcessors() = runTest {
        val processor =
            CompositeLogRecordProcessor(
                emptyList(),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testMultipleSpanProcessors() = runTest {
        val first = FakeLogRecordProcessor()
        val second = FakeLogRecordProcessor()
        val processor =
            CompositeLogRecordProcessor(
                listOf(first, second),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneProcessorFlushFails() = runTest {
        val first = FakeLogRecordProcessor(flushCode = { Failure })
        val second = FakeLogRecordProcessor()
        val processor =
            CompositeLogRecordProcessor(
                listOf(first, second),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Failure,
            Success,
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneProcessorShutdownFails() = runTest {
        val first = FakeLogRecordProcessor(shutdownCode = { Failure })
        val second = FakeLogRecordProcessor()
        val processor =
            CompositeLogRecordProcessor(
                listOf(first, second),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Failure,
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneProcessorThrowsInOnEmit() = runTest {
        val first = FakeLogRecordProcessor(action = { _, _ -> throw IllegalStateException() })
        val second = FakeLogRecordProcessor()
        val processor =
            CompositeLogRecordProcessor(
                listOf(first, second),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Success,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneProcessorThrowsInFlush() = runTest {
        val first = FakeLogRecordProcessor(flushCode = { throw IllegalStateException() })
        val second = FakeLogRecordProcessor()
        val processor =
            CompositeLogRecordProcessor(
                listOf(first, second),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Failure,
            Success,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneProcessorThrowsInShutdown() = runTest {
        val first = FakeLogRecordProcessor(shutdownCode = { throw IllegalStateException() })
        val second = FakeLogRecordProcessor()
        val processor =
            CompositeLogRecordProcessor(
                listOf(first, second),
                errorHandler
            )
        processor.assertReturnValuesMatch(
            Success,
            Failure,
        )
        assertTelemetryCapturedFailure(first, second)
    }

    private suspend fun CompositeLogRecordProcessor.assertReturnValuesMatch(
        flush: OperationResultCode,
        shutdown: OperationResultCode
    ) {
        assertEquals(shutdown, shutdown())
        assertEquals(flush, forceFlush())
        onEmit(fakeLogRecord, fakeContext)
    }

    private fun assertTelemetryCapturedSuccess(
        first: FakeLogRecordProcessor,
        second: FakeLogRecordProcessor
    ) {
        assertFalse(errorHandler.hasErrors())
        assertSame(fakeLogRecord, first.logs.single())
        assertSame(fakeLogRecord, second.logs.single())
    }

    private fun assertTelemetryCapturedFailure(
        first: FakeLogRecordProcessor,
        second: FakeLogRecordProcessor
    ) {
        assertTrue(errorHandler.hasErrors())
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertSame(fakeLogRecord, first.logs.single())
        assertSame(fakeLogRecord, second.logs.single())
    }
}
