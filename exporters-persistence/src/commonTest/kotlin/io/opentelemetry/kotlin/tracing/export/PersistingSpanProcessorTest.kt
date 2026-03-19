package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeTelemetryFileSystem
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class PersistingSpanProcessorTest {

    @Test
    fun testSpansExported() = runTest {
        val exporter1 = FakeSpanExporter()
        val exporter2 = FakeSpanExporter()

        val spanContext = FakeSpanContext.VALID
        val parentContext = FakeSpanContext(
            traceIdBytes = FakeSpanContext.VALID.traceIdBytes,
            spanIdBytes = ByteArray(8) { (it + 1).toByte() },
        )
        val span = FakeReadWriteSpan(
            name = "span",
            spanContext = spanContext,
            parent = parentContext,
            startTimestamp = 1_000_000L,
            endTimestamp = 2_000_000L,
            attributes = mapOf("key" to "value"),
        )

        val processor = createProcessor(
            exporters = listOf(exporter1, exporter2),
            processors = listOf(FakeSpanProcessor()),
        )
        processor.onEnd(span)

        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())

        listOf(exporter1, exporter2).forEach { exporter ->
            val exported = exporter.exports.single()
            assertEquals("span", exported.name)
            assertEquals(spanContext.traceId, exported.spanContext.traceId)
            assertEquals(spanContext.spanId, exported.spanContext.spanId)
            assertEquals(parentContext.spanId, exported.parent.spanId)
            assertEquals(1_000_000L, exported.startTimestamp)
            assertEquals(2_000_000L, exported.endTimestamp)
            assertEquals("value", exported.attributes["key"])
        }
    }

    @Test
    fun testProcessorMutation() = runTest {
        val expected = "override"
        val processor1 = FakeSpanProcessor(
            endingAction = { span -> span.setName("flibbet") }
        )
        val processor2 = FakeSpanProcessor(
            endingAction = { span -> span.setName(expected) }
        )
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            processors = listOf(processor1, processor2),
            exporters = listOf(exporter),
        )

        val span = FakeReadWriteSpan(name = "test")
        processor.onEnding(span)
        processor.onEnd(span)
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(expected, exporter.exports.single().name)
    }

    @Test
    fun testSpanBatching() = runTest {
        val batchCounts = mutableListOf<Int>()
        val exporter = FakeSpanExporter(
            exportReturnValue = { batch ->
                batchCounts.add(batch.size)
                Success
            }
        )
        val processor = createProcessor(
            exporters = listOf(exporter),
            processors = listOf(FakeSpanProcessor()),
            maxExportBatchSize = 2,
            scheduleDelayMs = 1,
        )

        repeat(5) {
            processor.onEnd(FakeReadWriteSpan(name = "span"))
        }
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())

        assertTrue(batchCounts.all { it <= 2 })
        assertEquals(5, exporter.exports.size)
    }

    @Test
    fun testExportAfterShutdown() = runTest {
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            processors = listOf(FakeSpanProcessor()),
            exporters = listOf(exporter),
            maxExportBatchSize = 1,
            scheduleDelayMs = 1,
        )

        val name = "span"
        processor.onEnd(FakeReadWriteSpan(name = name))
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        processor.onEnd(FakeReadWriteSpan(name = "after shutdown"))
        assertEquals(name, exporter.exports.first().name)
    }

    @Test
    fun testEmptyProcessorsList() = runTest {
        val exporter = FakeSpanExporter()
        assertFailsWith(UnsupportedOperationException::class) {
            createProcessor(
                exporters = listOf(exporter),
            )
        }
    }

    @Test
    fun testEmptyExportersList() = runTest {
        val mutatingProcessor = FakeSpanProcessor()
        assertFailsWith(UnsupportedOperationException::class) {
            createProcessor(
                processors = listOf(mutatingProcessor),
            )
        }
    }

    @Test
    fun testExporterFailurePropagates() = runTest {
        val fileSystem = FakeTelemetryFileSystem()
        val failingExporter = FakeSpanExporter(
            exportReturnValue = { Failure }
        )
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(failingExporter),
            processors = listOf(FakeSpanProcessor()),
        )

        val name = "span"
        processor.onEnd(FakeReadWriteSpan(name = name))
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertTrue(failingExporter.exports.any { it.name == name })
        assertTrue(fileSystem.list().isNotEmpty())
    }

    @Test
    fun testProcessorFlushFailurePropagates() = runTest {
        val failingProcessor = FakeSpanProcessor(
            flushCode = { Failure }
        )
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            processors = listOf(failingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Failure, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
    }

    @Test
    fun testProcessorShutdownFailurePropagates() = runTest {
        val failingProcessor = FakeSpanProcessor(
            shutdownCode = { Failure }
        )
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            processors = listOf(failingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Success, processor.forceFlush())
        assertEquals(Failure, processor.shutdown())
    }

    @Test
    fun testProcessorFlushExceptionReturnsFailure() = runTest {
        val throwingProcessor = FakeSpanProcessor(
            flushCode = { error("flush exception") }
        )
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            processors = listOf(throwingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Failure, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
    }

    @Test
    fun testProcessorShutdownExceptionReturnsFailure() = runTest {
        val throwingProcessor = FakeSpanProcessor(
            shutdownCode = { error("shutdown exception") }
        )
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            processors = listOf(throwingProcessor),
            exporters = listOf(exporter),
        )

        assertEquals(Success, processor.forceFlush())
        assertEquals(Failure, processor.shutdown())
    }

    @Test
    fun testOnEndExceptionInProcessorDoesNotCrash() = runTest {
        val throwingProcessor = FakeSpanProcessor(
            endAction = { _ -> error("onEnd exception") }
        )
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            processors = listOf(throwingProcessor),
            exporters = listOf(exporter),
            maxExportBatchSize = 1,
            scheduleDelayMs = 1,
        )

        processor.onEnd(FakeReadWriteSpan(name = "first"))
        processor.onEnd(FakeReadWriteSpan(name = "second"))
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertEquals(2, throwingProcessor.endCalls.size)
    }

    @Test
    fun testForceFlushWithinTimeout() = runTest {
        val delayingProcessor = DelayingSpanProcessor(flushDelayMs = 1000)
        val exporter = FakeSpanExporter()
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
        val delayingProcessor = DelayingSpanProcessor(flushDelayMs = 3000)
        val exporter = FakeSpanExporter()
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
        val delayingProcessor = DelayingSpanProcessor(shutdownDelayMs = 3000)
        val exporter = FakeSpanExporter()
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
        val delayingProcessor = DelayingSpanProcessor(shutdownDelayMs = 6000)
        val exporter = FakeSpanExporter()
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
        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeSpanProcessor()),
        )

        processor.onEnd(FakeReadWriteSpan(name = "span"))
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertTrue(exporter.exports.isNotEmpty())
        assertTrue(fileSystem.list().isEmpty())
    }

    /**
     * Asserts that the filesystem write happens before export.
     */
    @Test
    fun testWriteBeforeExportOrdering() = runTest {
        val fileSystem = FakeTelemetryFileSystem()
        var files: List<String> = emptyList()
        val exporter = FakeSpanExporter(
            exportReturnValue = { _ ->
                files = fileSystem.list()
                Success
            },
        )
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeSpanProcessor()),
        )

        processor.onEnd(FakeReadWriteSpan(name = "span"))
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
        val exporter = FakeSpanExporter(exportReturnValue = { Failure })
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeSpanProcessor()),
        )

        processor.onEnd(FakeReadWriteSpan(name = "span"))
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())

        assertTrue(fileSystem.list().isNotEmpty())

        // create new processor with succeeding export
        val session2Exporter = FakeSpanExporter()
        val session2Processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(session2Exporter),
            processors = listOf(FakeSpanProcessor()),
        )

        session2Processor.onEnd(FakeReadWriteSpan(name = "other"))
        assertEquals(Success, session2Processor.forceFlush())
        assertEquals(Success, session2Processor.shutdown())

        val exportedNames = session2Exporter.exports.map { it.name }
        assertTrue("other" in exportedNames)
        assertTrue("span" in exportedNames)
    }

    @Test
    fun testFlushExportsPersistedSpans() = runTest {
        val fileSystem = FakeTelemetryFileSystem()

        // store a span that fails to export
        val failingExporter = FakeSpanExporter(exportReturnValue = { Failure })
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(failingExporter),
            processors = listOf(FakeSpanProcessor()),
        )
        processor.onEnd(FakeReadWriteSpan(name = "persisted"))
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())
        assertTrue(fileSystem.list().isNotEmpty())

        // new processor with succeeding exporter recovers the persisted span
        val successExporter = FakeSpanExporter()
        val recoveryProcessor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(successExporter),
            processors = listOf(FakeSpanProcessor()),
        )
        assertEquals(Success, recoveryProcessor.forceFlush())
        assertEquals(Success, recoveryProcessor.shutdown())

        assertTrue(successExporter.exports.any { it.name == "persisted" })
        assertTrue(fileSystem.list().isEmpty())
    }

    @Test
    fun testFlushContinuesPastFailedSpans() = runTest {
        val fileSystem = FakeTelemetryFileSystem()

        // store two spans using two separate processors (one span each)
        val storingProcessor1 = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(FakeSpanExporter(exportReturnValue = { Failure })),
            processors = listOf(FakeSpanProcessor()),
        )
        storingProcessor1.onEnd(FakeReadWriteSpan(name = "span-1"))
        assertEquals(Success, storingProcessor1.forceFlush())
        assertEquals(Success, storingProcessor1.shutdown())

        val storingProcessor2 = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(FakeSpanExporter(exportReturnValue = { Failure })),
            processors = listOf(FakeSpanProcessor()),
        )
        storingProcessor2.onEnd(FakeReadWriteSpan(name = "span-2"))
        assertEquals(Success, storingProcessor2.forceFlush())
        assertEquals(Success, storingProcessor2.shutdown())

        assertEquals(2, fileSystem.list().size)

        // flush with an always-failing exporter to verify both spans are attempted
        var exportCount = 0
        val alwaysFailExporter = FakeSpanExporter(exportReturnValue = {
            exportCount++
            Failure
        })
        val flushProcessor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(alwaysFailExporter),
            processors = listOf(FakeSpanProcessor()),
        )
        assertEquals(Success, flushProcessor.forceFlush())

        // both spans should have been attempted during the single flushPersisted() call
        assertEquals(2, exportCount)
        assertEquals(2, fileSystem.list().size)

        flushProcessor.shutdown()
    }

    @Test
    fun testFlushDeletesCorruptedSpans() = runTest {
        val fileSystem = FakeTelemetryFileSystem()

        // store a span
        val storingProcessor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(FakeSpanExporter(exportReturnValue = { Failure })),
            processors = listOf(FakeSpanProcessor()),
        )
        storingProcessor.onEnd(FakeReadWriteSpan(name = "corrupted"))
        assertEquals(Success, storingProcessor.forceFlush())
        assertEquals(Success, storingProcessor.shutdown())
        assertTrue(fileSystem.list().isNotEmpty())

        // make reads fail, simulating bad data
        fileSystem.failReads = true

        val exporter = FakeSpanExporter()
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeSpanProcessor()),
        )
        assertEquals(Success, processor.forceFlush())
        assertEquals(Success, processor.shutdown())

        // bad span should be deleted, exporter should not be called
        assertTrue(fileSystem.list().isEmpty())
        assertTrue(exporter.exports.isEmpty())
    }

    @Test
    fun testConcurrentFlushSafety() = runTest {
        val fileSystem = FakeTelemetryFileSystem()
        var exportCount = 0
        val exporter = FakeSpanExporter(
            exportReturnValue = { batch ->
                exportCount += batch.size
                Success
            }
        )
        val processor = createProcessor(
            fileSystem = fileSystem,
            exporters = listOf(exporter),
            processors = listOf(FakeSpanProcessor()),
        )

        repeat(3) {
            processor.onEnd(FakeReadWriteSpan(name = "span-$it"))
        }

        // run two concurrent forceFlush calls
        val flush1 = async { processor.forceFlush() }
        val flush2 = async { processor.forceFlush() }
        assertEquals(Success, flush1.await())
        assertEquals(Success, flush2.await())
        assertEquals(Success, processor.shutdown())

        assertEquals(3, exportCount)
        assertTrue(fileSystem.list().isEmpty())
    }

    @Test
    fun testShutdownStopsSpanProcessing() = runTest {
        val exporter = FakeSpanExporter()
        val processor = FakeSpanProcessor()

        val span = FakeReadWriteSpan()

        val persistingProcessor = createProcessor(
            exporters = listOf(exporter),
            processors = listOf(processor),
        )

        persistingProcessor.onStart(span, FakeContext())
        persistingProcessor.onEnding(span)
        persistingProcessor.onEnd(span)
        assertEquals(1, processor.startCalls.size)
        assertEquals(1, processor.endingCalls.size)
        assertEquals(1, processor.endCalls.size)

        assertEquals(Success, persistingProcessor.shutdown())
        assertEquals(Success, persistingProcessor.shutdown())

        persistingProcessor.onStart(span, FakeContext())
        persistingProcessor.onEnding(span)
        persistingProcessor.onEnd(span)
        assertEquals(1, processor.startCalls.size)
        assertEquals(1, processor.endingCalls.size)
        assertEquals(1, processor.endCalls.size)
    }

    private fun TestScope.createProcessor(
        fileSystem: FakeTelemetryFileSystem = FakeTelemetryFileSystem(),
        processors: List<SpanProcessor> = emptyList(),
        exporters: List<SpanExporter> = emptyList(),
        maxExportBatchSize: Int = 512,
        scheduleDelayMs: Long = 5000,
    ): SpanProcessor {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val cfg = FakeTraceExportConfig()
        val processor = when {
            processors.isEmpty() -> throw UnsupportedOperationException("Processors cannot be empty")
            processors.size == 1 -> processors.single()
            else -> cfg.compositeSpanProcessor(*processors.toTypedArray())
        }
        val exporter = when {
            exporters.isEmpty() -> throw UnsupportedOperationException("Exporters cannot be empty")
            exporters.size == 1 -> exporters.single()
            else -> cfg.compositeSpanExporter(*exporters.toTypedArray())
        }
        return cfg.persistingSpanProcessorImpl(
            processor = processor,
            exporter = exporter,
            fileSystem = fileSystem,
            maxExportBatchSize = maxExportBatchSize,
            scheduleDelayMs = scheduleDelayMs,
            dispatcher = dispatcher,
        )
    }

    private class FakeTraceExportConfig(override val clock: Clock = FakeClock()) :
        TraceExportConfigDsl

    private class DelayingSpanProcessor(
        private val flushDelayMs: Long = 0,
        private val shutdownDelayMs: Long = 0,
    ) : SpanProcessor {

        override fun onStart(span: ReadWriteSpan, parentContext: Context) {}
        override fun onEnding(span: ReadWriteSpan) {}
        override fun onEnd(span: ReadableSpan) {}
        override fun isStartRequired(): Boolean = false
        override fun isEndRequired(): Boolean = true

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
