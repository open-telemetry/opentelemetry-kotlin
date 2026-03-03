package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeTelemetryFileSystem
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PersistingLogRecordProcessorTest {

    private val context = FakeContext()

    @Test
    fun testLogsExported() = runTest {
        val exporter1 = FakeLogRecordExporter()
        val exporter2 = FakeLogRecordExporter()

        val spanContext = FakeSpanContext.VALID
        val log = FakeReadWriteLogRecord(
            body = "log",
            timestamp = 1_000_000L,
            observedTimestamp = 2_000_000L,
            severityNumber = SeverityNumber.WARN,
            severityText = "warning",
            attributes = mapOf("key" to "value"),
            spanContext = spanContext,
        )

        val processor = createProcessor(
            exporters = listOf(exporter1, exporter2),
            processors = listOf(FakeLogRecordProcessor()),
        )
        processor.onEmit(log, context)

        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())

        listOf(exporter1, exporter2).forEach { exporter ->
            val exported = exporter.logs.single()
            assertEquals("log", exported.body)
            assertEquals(1_000_000L, exported.timestamp)
            assertEquals(2_000_000L, exported.observedTimestamp)
            assertEquals(SeverityNumber.WARN, exported.severityNumber)
            assertEquals("warning", exported.severityText)
            assertEquals("value", exported.attributes["key"])
            assertEquals(spanContext.traceId, exported.spanContext.traceId)
            assertEquals(spanContext.spanId, exported.spanContext.spanId)
        }
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
            processors = listOf(FakeLogRecordProcessor()),
            maxExportBatchSize = 2,
            scheduleDelayMs = 1,
        )

        repeat(5) {
            processor.onEmit(FakeReadWriteLogRecord(body = "log"), context)
        }
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())

        assertTrue(batchCounts.all { it <= 2 })
        assertEquals(5, exporter.logs.size)
    }

    @Test
    fun testExportAfterShutdown() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            processors = listOf(FakeLogRecordProcessor()),
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
        assertFailsWith(UnsupportedOperationException::class) {
            createProcessor(
                exporters = listOf(exporter),
            )
        }
    }

    @Test
    fun testEmptyExportersList() = runTest {
        val mutatingProcessor = FakeLogRecordProcessor()
        assertFailsWith(UnsupportedOperationException::class) {
            createProcessor(
                processors = listOf(mutatingProcessor),
            )
        }
    }

    @Test
    fun testExporterFailurePropagates() = runTest {
        val fileSystem = FakeTelemetryFileSystem()
        val failingExporter = FakeLogRecordExporter(
            action = { Failure }
        )
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(failingExporter),
            processors = listOf(FakeLogRecordProcessor()),
        )

        val body = "log"
        processor.onEmit(FakeReadWriteLogRecord(body = body), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(body, failingExporter.logs.single().body)
        assertTrue(
            fileSystem.list().isNotEmpty(),
            "Persisted file should be retained when the export fails",
        )
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

    /**
     * Checks that when the filesystem cannot be written telemetry is still exported
     * in a best-effort attempt
     */
    @Test
    fun testFilesystemUnwritable() = runTest {
        val fileSystem = FakeTelemetryFileSystem().apply { failWrites = true }
        val exporter = FakeLogRecordExporter()
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeLogRecordProcessor()),
        )

        processor.onEmit(FakeReadWriteLogRecord(body = "log"), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertTrue(exporter.logs.isNotEmpty())
        assertTrue(fileSystem.list().isEmpty())
    }

    /**
     * Asserts that the filesystem write happens before export.
     */
    @Test
    fun testWriteBeforeExportOrdering() = runTest {
        val fileSystem = FakeTelemetryFileSystem()
        var files: List<String> = emptyList()
        val exporter = FakeLogRecordExporter(
            action = { _ ->
                files = fileSystem.list()
                Success
            },
        )
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeLogRecordProcessor()),
        )

        processor.onEmit(FakeReadWriteLogRecord(body = "log"), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertTrue(files.isNotEmpty())
    }

    /**
     * Asserts that data persisted by one processor can be recovered by another processor that
     * shares the file system. This effectively simulates what happens after process termination.
     */
    @Test
    fun testProcessTerminationRecovery() = runTest {
        val fileSystem = FakeTelemetryFileSystem()

        // emit telemetry but fail to export
        val exporter = FakeLogRecordExporter(action = { Failure })
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeLogRecordProcessor()),
        )

        processor.onEmit(FakeReadWriteLogRecord(body = "log"), context)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertTrue(fileSystem.list().isNotEmpty())

        // create new processor with succeeding export
        val otherExporter = FakeLogRecordExporter()
        val otherProcessor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(otherExporter),
            processors = listOf(FakeLogRecordProcessor()),
        )

        otherProcessor.onEmit(FakeReadWriteLogRecord(body = "other"), context)
        assertEquals(Success, otherProcessor.forceFlush())
        assertEquals(Success, otherProcessor.shutdown())

        val exportedBodies = otherExporter.logs.map { it.body }
        assertTrue("other" in exportedBodies)

        // TODO: future: alter the assertion when persisted records are exported.
        assertFalse("log" in exportedBodies)
    }

    private fun TestScope.createProcessor(
        fileSystem: FakeTelemetryFileSystem = FakeTelemetryFileSystem(),
        processors: List<LogRecordProcessor> = emptyList(),
        exporters: List<LogRecordExporter> = emptyList(),
        maxExportBatchSize: Int = 512,
        scheduleDelayMs: Long = 5000,
    ): LogRecordProcessor {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val cfg = FakeLogExportConfig()
        val processor = when {
            processors.isEmpty() -> throw UnsupportedOperationException("Processors cannot be empty")
            processors.size == 1 -> processors.single()
            else -> cfg.compositeLogRecordProcessor(*processors.toTypedArray())
        }
        val exporter = when {
            exporters.isEmpty() -> throw UnsupportedOperationException("Exporters cannot be empty")
            exporters.size == 1 -> exporters.single()
            else -> cfg.compositeLogRecordExporter(*exporters.toTypedArray())
        }
        return cfg.persistingLogRecordProcessorImpl(
            processor = processor,
            exporter = exporter,
            fileSystem = fileSystem,
            clock = FakeClock(),
            maxExportBatchSize = maxExportBatchSize,
            scheduleDelayMs = scheduleDelayMs,
            dispatcher = dispatcher,
        )
    }

    private class FakeLogExportConfig(override val clock: Clock = FakeClock()) : LogExportConfigDsl

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
