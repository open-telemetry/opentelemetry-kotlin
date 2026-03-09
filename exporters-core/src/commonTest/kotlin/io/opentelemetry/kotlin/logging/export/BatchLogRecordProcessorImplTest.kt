package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class BatchLogRecordProcessorImplTest {

    private lateinit var exporter: FakeLogRecordExporter
    private lateinit var processor: BatchLogRecordProcessorImpl

    @BeforeTest
    fun setup() {
        exporter = FakeLogRecordExporter()
        processor = BatchLogRecordProcessorImpl(
            exporter = exporter,
            maxQueueSize = 100,
            scheduleDelayMs = 1,
            exportTimeoutMs = 1000,
            maxExportBatchSize = 10,
        )
    }

    @Test
    fun testOnEmitNoOpAfterShutdown() = runTest {
        processor.shutdown()
        advanceUntilIdle()

        val log = FakeReadWriteLogRecord()
        processor.onEmit(log, FakeContext())
        advanceUntilIdle()

        assertTrue(exporter.logs.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testEnabledReturnsFalseAfterShutdown() = runTest {
        assertTrue(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
        processor.shutdown()
        assertFalse(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        processor.shutdown()
        advanceUntilIdle()
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }
}
