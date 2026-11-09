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
    fun testConcurrentFlushAndSubmissionDoNotDeadlock() = runTest {
        val exporter = OrderTrackingSpanExporter()
        exporter.delayMs = 10
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        repeat(10) { processor.onEnd(FakeReadWriteSpan(name = "span_$it")) }

        // Multiple concurrent flushes while more spans are submitted must all succeed.
        val flushResults = listOf(
            async { processor.forceFlush() },
            async { processor.forceFlush() },
            async { processor.forceFlush() },
        )
        // Start the flushes first so each snapshots pending exports before the
        // extra submissions below race the in-flight flushes.
        runCurrent()
        repeat(10) { processor.onEnd(FakeReadWriteSpan(name = "extra_$it")) }
        advanceUntilIdle()

        assertTrue(flushResults.awaitAll().all { it == OperationResultCode.Success })
        assertTrue(exporter.exports.size >= 10)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testFlushAndShutdownWaitForExportsBeforeExporterCalls() = runTest {
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        fun processor(exporter: SpanExporter) = SimpleSpanProcessor(exporter, scope, NoopSdkErrorHandler)

        // One blocked export plus one queued export must finish before exporter calls.
        suspend fun drainScenario(shutdown: Boolean): Pair<OrderTrackingSpanExporter, OperationResultCode> {
            val exporter = OrderTrackingSpanExporter(gated = true)
            val p = processor(exporter)
            p.onEnd(FakeReadWriteSpan(name = "span1"))
            runCurrent()
            p.onEnd(FakeReadWriteSpan(name = "span2"))
            runCurrent()
            val result = async { if (shutdown) { p.shutdown() } else { p.forceFlush() } }
            runCurrent()
            exporter.release()
            advanceUntilIdle()
            return exporter to result.await()
        }

        val (flushExporter, flushResult) = drainScenario(shutdown = false)
        assertEquals(OperationResultCode.Success, flushResult)
        assertEquals(listOf("export:span1", "export:span2", "forceFlush"), flushExporter.events)

        val (shutdownExporter, shutdownResult) = drainScenario(shutdown = true)
        assertEquals(OperationResultCode.Success, shutdownResult)
        assertEquals(
            listOf("export:span1", "export:span2", "forceFlush", "shutdown"),
            shutdownExporter.events,
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testExportTimeoutReleasesMutexAndDoesNotBlockProcessor() = runTest {
        val exporter = OrderTrackingSpanExporter()
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

    /** Exporter recording export completions and lifecycle calls in [events]. */
    private class OrderTrackingSpanExporter(private val gated: Boolean = false) : SpanExporter {
        val events = mutableListOf<String>()
        val exports: List<String>
            get() = events.filter { it.startsWith("export:") }.map { it.removePrefix("export:") }
        private val release = CompletableDeferred<Unit>()
        var delayMs: Long = 0

        fun release() = release.complete(Unit)

        override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
            if (gated) {
                release.await()
            }
            delay(delayMs)
            events += "export:${telemetry.first().name}"
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
}
