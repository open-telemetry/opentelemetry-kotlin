package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeTelemetryFileSystem
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class PersistingLogRecordProcessorTest {

    private val context = FakeContext()

    @Test
    fun testLogsExported() = runTest {
        val exporter1 = FakeLogRecordExporter()
        val exporter2 = FakeLogRecordExporter()
        val processor = createProcessor(
            exporters = listOf(exporter1, exporter2),
        )

        val body = "log"
        val log = FakeReadWriteLogRecord(body = body)
        processor.onEmit(log, context)

        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(body, exporter1.logs.single().body)
        assertEquals(body, exporter2.logs.single().body)
    }

    @Test
    fun testProcessorMutation() = runTest {
        val expected = "override"
        val processor1 = FakeLogRecordProcessor(
            action = { log, _ ->
                log.body = "flibbet"
            }
        )
        val processor2 = FakeLogRecordProcessor(
            action = { log, _ ->
                log.body = expected
            }
        )
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(processor1, processor2),
            exporters = listOf(exporter),
        )

        val log = FakeReadWriteLogRecord(body = "test")
        processor.onEmit(log, context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(expected, exporter.logs.single().body)
    }

    @Test
    fun testLogBatching() = runTest {
        val batchCounts = mutableListOf<Int>()
        val exporter = FakeLogRecordExporter(
            action = { batch ->
                batchCounts.add(batch.size)
                Success
            }
        )
        val processor = createProcessor(
            exporters = listOf(exporter),
            maxExportBatchSize = 2,
            scheduleDelayMs = 1,
        )

        repeat(4) {
            processor.onEmit(FakeReadWriteLogRecord(body = "log"), context)
        }
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())

        assertTrue(exporter.logs.isNotEmpty())
        assertTrue(batchCounts.all { it <= 2 })
    }

    @Test
    fun testExportAfterShutdown() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            exporters = listOf(exporter),
            maxExportBatchSize = 1,
            scheduleDelayMs = 1,
        )

        val body = "log"
        processor.onEmit(FakeReadWriteLogRecord(body = body), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        processor.onEmit(FakeReadWriteLogRecord(body = "after shutdown"), context)
        assertEquals(body, exporter.logs.first().body)
    }

    @Test
    fun testEmptyProcessorsList() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            exporters = listOf(exporter),
        )

        val body = "log"
        processor.onEmit(FakeReadWriteLogRecord(body = body), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(body, exporter.logs.single().body)
    }

    @Test
    fun testEmptyExportersList() = runTest {
        val mutatingProcessor = FakeLogRecordProcessor()
        val processor = createProcessor(
            processors = listOf(mutatingProcessor),
        )

        val body = "log"
        processor.onEmit(FakeReadWriteLogRecord(body = body), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(body, mutatingProcessor.logs.single().body)
    }

    @Test
    fun testExporterFailurePropagates() = runTest {
        val failingExporter = FakeLogRecordExporter(
            action = { Failure }
        )
        val processor = createProcessor(
            exporters = listOf(failingExporter),
        )

        val body = "log"
        processor.onEmit(FakeReadWriteLogRecord(body = body), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(body, failingExporter.logs.single().body)
    }

    @Test
    fun testProcessorFlushFailurePropagates() = runTest {
        val failingProcessor = FakeLogRecordProcessor(
            flushCode = { Failure }
        )
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(failingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Failure, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
    }

    @Test
    fun testProcessorShutdownFailurePropagates() = runTest {
        val failingProcessor = FakeLogRecordProcessor(
            shutdownCode = { Failure }
        )
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(failingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Success, processor.forceFlush())
        assertEquals(Failure, processor.shutdown())
    }

    @Test
    fun testProcessorFlushExceptionReturnsFailure() = runTest {
        val throwingProcessor = FakeLogRecordProcessor(
            flushCode = { error("flush exception") }
        )
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(throwingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Failure, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
    }

    @Test
    fun testProcessorShutdownExceptionReturnsFailure() = runTest {
        val throwingProcessor = FakeLogRecordProcessor(
            shutdownCode = { error("shutdown exception") }
        )
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(throwingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Success, processor.forceFlush())
        assertEquals(Failure, processor.shutdown())
    }

    @Test
    fun testOnEmitExceptionInProcessorDoesNotCrash() = runTest {
        val throwingProcessor = FakeLogRecordProcessor(
            action = { _, _ -> error("onEmit exception") }
        )
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(throwingProcessor),
            exporters = listOf(exporter),
            maxExportBatchSize = 1,
            scheduleDelayMs = 1,
        )

        processor.onEmit(FakeReadWriteLogRecord(body = "first"), context)
        processor.onEmit(FakeReadWriteLogRecord(body = "second"), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(2, throwingProcessor.logs.size)
    }

    @Test
    fun testForceFlushWithinTimeout() = runTest {
        val delayingProcessor = DelayingLogRecordProcessor(flushDelayMs = 1000)
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(delayingProcessor),
            exporters = listOf(exporter),
        )

        val resultDeferred = async { processor.forceFlush() }
        advanceTimeBy(1500)
        val result = resultDeferred.await()

        assertEquals(Success, result)
        processor.shutdown()
    }

    @Test
    fun testForceFlushOverTimeout() = runTest {
        val delayingProcessor = DelayingLogRecordProcessor(flushDelayMs = 3000)
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(delayingProcessor),
            exporters = listOf(exporter),
        )

        val resultDeferred = async { processor.forceFlush() }
        advanceTimeBy(2500)
        val result = resultDeferred.await()

        assertEquals(Failure, result)
        processor.shutdown()
    }

    @Test
    fun testShutdownWithinTimeout() = runTest {
        val delayingProcessor = DelayingLogRecordProcessor(shutdownDelayMs = 3000)
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(delayingProcessor),
            exporters = listOf(exporter),
        )

        val resultDeferred = async { processor.shutdown() }
        advanceTimeBy(4000)
        val result = resultDeferred.await()

        assertEquals(Success, result)
    }

    @Test
    fun testShutdownOverTimeout() = runTest {
        val delayingProcessor = DelayingLogRecordProcessor(shutdownDelayMs = 6000)
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(delayingProcessor),
            exporters = listOf(exporter),
        )

        val resultDeferred = async { processor.shutdown() }
        advanceTimeBy(5500)
        val result = resultDeferred.await()

        assertEquals(Failure, result)
    }

    private fun TestScope.createProcessor(
        processors: List<LogRecordProcessor> = emptyList(),
        exporters: List<LogRecordExporter> = emptyList(),
        maxExportBatchSize: Int = 512,
        scheduleDelayMs: Long = 5000,
    ): LogRecordProcessor {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return createPersistingLogRecordProcessor(
            processors = processors,
            exporters = exporters,
            fileSystem = FakeTelemetryFileSystem(),
            clock = FakeClock(),
            maxExportBatchSize = maxExportBatchSize,
            scheduleDelayMs = scheduleDelayMs,
            dispatcher = dispatcher,
        )
    }

    @OptIn(ExperimentalApi::class)
    private class DelayingLogRecordProcessor(
        private val flushDelayMs: Long = 0,
        private val shutdownDelayMs: Long = 0,
    ) : LogRecordProcessor {

        val logs = mutableListOf<ReadWriteLogRecord>()

        override fun onEmit(log: ReadWriteLogRecord, context: Context) {
            logs.add(log)
        }

        override suspend fun forceFlush(): OperationResultCode {
            if (flushDelayMs > 0) {
                delay(flushDelayMs)
            }
            return Success
        }

        override suspend fun shutdown(): OperationResultCode {
            if (shutdownDelayMs > 0) {
                delay(shutdownDelayMs)
            }
            return Success
        }
    }
}
