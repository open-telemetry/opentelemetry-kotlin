package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class BatchSpanProcessorImplTest {

    private lateinit var exporter: FakeSpanExporter
    private lateinit var processor: BatchSpanProcessorImpl

    @BeforeTest
    fun setup() {
        exporter = FakeSpanExporter()
        processor = BatchSpanProcessorImpl(
            exporter = exporter,
            maxQueueSize = 100,
            scheduleDelayMs = 1,
            exportTimeoutMs = 1000,
            maxExportBatchSize = 10,
        )
    }

    @Test
    fun testOnEndNoOpAfterShutdown() = runTest {
        processor.shutdown()
        advanceUntilIdle()

        val span = FakeReadWriteSpan()
        processor.onEnd(span)
        advanceUntilIdle()

        assertTrue(exporter.exports.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        processor.shutdown()
        advanceUntilIdle()
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }
}
