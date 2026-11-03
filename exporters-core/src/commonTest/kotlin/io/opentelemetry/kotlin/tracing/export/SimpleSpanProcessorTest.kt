package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.BatchTelemetryDefaults
import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.data.SpanData
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

internal class SimpleSpanProcessorTest {

    @Test
    fun testSpanProcessorDefaults() = runTest {
        val processor = FakeTraceExportConfig().simpleSpanProcessor(FakeSpanExporter())
        assertFalse(processor.isStartRequired())
        assertTrue(processor.isEndRequired())
        assertFalse(processor.isOnEndingRequired())
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSpanProcessorExport() = runTest {
        val exporter = FakeSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)
        val span = FakeReadWriteSpan()
        processor.onStart(span, FakeContext())
        processor.onEnd(span)

        val export = exporter.exports.single()
        assertEquals(span.name, export.name)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSpanProcessorSkipsUnsampledSpan() = runTest {
        val exporter = FakeSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)
        val span = FakeReadWriteSpan(
            spanContext = FakeSpanContext(traceFlags = FakeTraceFlags(isSampled = false))
        )

        processor.onEnd(span)

        assertTrue(exporter.exports.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testOnEndNoOpAfterShutdown() = runTest {
        val exporter = FakeSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)
        processor.shutdown()

        val span = FakeReadWriteSpan()
        processor.onEnd(span)
        assertTrue(exporter.exports.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        val exporter = FakeSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val exporter = FakeSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)
        processor.shutdown()

        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testConcurrentExportsAreSerialized() = runTest {
        val exporter = SuspendingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        repeat(5) { processor.onEnd(FakeReadWriteSpan(name = "span_$it")) }
        advanceUntilIdle()

        assertFalse(exporter.observedConcurrentExport)
        assertEquals(List(5) { "span_$it" }, exporter.exports)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testForceFlushWaitsForPendingExports() = runTest {
        val exporter = SlowSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        processor.onEnd(FakeReadWriteSpan(name = "span2"))

        // Force flush should wait for exports to complete
        assertEquals(OperationResultCode.Success, processor.forceFlush())
        assertEquals(2, exporter.exports.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testShutdownWaitsForPendingExports() = runTest {
        val exporter = SlowSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        processor.onEnd(FakeReadWriteSpan(name = "span2"))

        // Shutdown should wait for exports to complete
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(2, exporter.exports.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testConcurrentForceFlush() = runTest {
        val exporter = SlowSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))

        // Multiple concurrent forceFlush calls should all succeed
        val flushResults = listOf(
            async { processor.forceFlush() },
            async { processor.forceFlush() },
            async { processor.forceFlush() }
        ).awaitAll()

        assertTrue(flushResults.all { it == OperationResultCode.Success })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testExportFailureDoesNotCrashProcessor() = runTest {
        val exporter = FailingSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle()

        // Processor should still work after failure
        processor.onEnd(FakeReadWriteSpan(name = "span2"))
        advanceUntilIdle()

        assertEquals(2, exporter.exportCallCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testForceFlushWaitsForExportAlreadyExecuting() = runTest {
        val exporter = BlockingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent() // export starts and blocks holding the mutex

        val flushResult = async { processor.forceFlush() }
        runCurrent() // forceFlush snapshots the in-flight job and waits

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        assertEquals(listOf("span1"), exporter.exports)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testForceFlushWaitsForExportQueuedBehindMutex() = runTest {
        val exporter = BlockingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent() // span1 export starts and holds the mutex

        processor.onEnd(FakeReadWriteSpan(name = "span2"))
        runCurrent() // span2 export is queued behind the mutex

        val flushResult = async { processor.forceFlush() }
        runCurrent() // forceFlush waits for both in-flight and queued jobs

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        assertEquals(listOf("span1", "span2"), exporter.exports)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testForceFlushCallsExporterFlushOnlyAfterExportsFinish() = runTest {
        val exporter = OrderTrackingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent() // export starts and blocks

        val flushResult = async { processor.forceFlush() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        assertEquals(listOf("export:span1", "forceFlush"), exporter.events)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testShutdownWaitsForExportAlreadyExecuting() = runTest {
        val exporter = BlockingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent() // export starts and blocks

        val shutdownResult = async { processor.shutdown() }
        runCurrent() // shutdown waits for the in-flight job

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, shutdownResult.await())
        assertEquals(listOf("span1"), exporter.exports)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testShutdownWaitsForExportsQueuedBehindMutex() = runTest {
        val exporter = BlockingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent()

        processor.onEnd(FakeReadWriteSpan(name = "span2"))
        runCurrent() // span2 is queued behind the mutex

        val shutdownResult = async { processor.shutdown() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, shutdownResult.await())
        assertEquals(listOf("span1", "span2"), exporter.exports)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testShutdownCallsExporterFlushAndShutdownAfterDrain() = runTest {
        val exporter = OrderTrackingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        runCurrent() // export starts and blocks

        val shutdownResult = async { processor.shutdown() }
        runCurrent()

        exporter.release()
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, shutdownResult.await())
        assertEquals(listOf("export:span1", "forceFlush", "shutdown"), exporter.events)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testExportTimeoutReleasesMutexAndDoesNotBlockProcessor() = runTest {
        val exporter = ConfigurableDelaySpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        exporter.delayMs = BatchTelemetryDefaults.EXPORT_TIMEOUT_MS + 1000
        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle() // export times out

        assertTrue(exporter.exports.isEmpty()) // span1 was not exported (timed out)

        // A subsequent export should succeed — the mutex must have been released
        exporter.delayMs = 0
        processor.onEnd(FakeReadWriteSpan(name = "span2"))
        advanceUntilIdle()

        assertEquals(listOf("span2"), exporter.exports)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testExporterReturningFailureDoesNotBlockProcessor() = runTest {
        val exporter = FailureResultSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        processor.onEnd(FakeReadWriteSpan(name = "span1"))
        advanceUntilIdle()

        assertEquals(1, exporter.exportCount)

        // Processor should still work after a failure return
        processor.onEnd(FakeReadWriteSpan(name = "span2"))
        advanceUntilIdle()

        assertEquals(2, exporter.exportCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testConcurrentSubmissionAndFlushDoNotDeadlock() = runTest {
        val exporter = SlowSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        repeat(10) { processor.onEnd(FakeReadWriteSpan(name = "span_$it")) }

        val flushResult = async { processor.forceFlush() }
        // Submit more while the flush is waiting
        repeat(10) { processor.onEnd(FakeReadWriteSpan(name = "extra_$it")) }
        advanceUntilIdle()

        assertEquals(OperationResultCode.Success, flushResult.await())
        // The flush waits for the 10 original exports; the 10 "extra" exports
        // may or may not be included depending on timing, but all eventually run
        assertTrue(exporter.exports.size >= 10)
    }

    private class SuspendingSpanExporter : SpanExporter {
        val exports: MutableList<String> = mutableListOf()
        var observedConcurrentExport: Boolean = false
        private var activeExports: Int = 0

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            activeExports++
            if (activeExports > 1) {
                observedConcurrentExport = true
            }
            delay(EXPORT_DURATION_MS)
            exports += telemetry.map(SpanData::name)
            activeExports--
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success

        private companion object {
            const val EXPORT_DURATION_MS = 2L
        }
    }

    private class SlowSpanExporter : SpanExporter {
        val exports: MutableList<String> = mutableListOf()

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            delay(10)
            exports += telemetry.map(SpanData::name)
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    private class FailingSpanExporter : SpanExporter {
        var exportCallCount = 0

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            exportCallCount++
            error("Export failed")
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    /** Blocks the first export until [release] is completed, then records the span name. */
    private class BlockingSpanExporter : SpanExporter {
        val exports = mutableListOf<String>()
        private val release = CompletableDeferred<Unit>()

        fun release() = release.complete(Unit)

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            release.await()
            exports += telemetry.map(SpanData::name)
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    /** Records the order of export, forceFlush, and shutdown calls. */
    private class OrderTrackingSpanExporter : SpanExporter {
        val events = mutableListOf<String>()
        private val release = CompletableDeferred<Unit>()

        fun release() = release.complete(Unit)

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            events += "export:${telemetry.first().name}"
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
    private class ConfigurableDelaySpanExporter : SpanExporter {
        val exports = mutableListOf<String>()
        var delayMs: Long = 0

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            if (delayMs > 0) {
                delay(delayMs)
            }
            exports += telemetry.map(SpanData::name)
            return OperationResultCode.Success
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }

    /** Always returns [OperationResultCode.Failure] from export. */
    private class FailureResultSpanExporter : SpanExporter {
        var exportCount = 0

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            exportCount++
            return OperationResultCode.Failure
        }

        override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
        override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success
    }
}
