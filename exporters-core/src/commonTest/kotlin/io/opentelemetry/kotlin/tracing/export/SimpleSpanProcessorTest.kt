package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
        val processor = SimpleSpanProcessor(exporter, scope)
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
        val processor = SimpleSpanProcessor(exporter, scope)
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
        val processor = SimpleSpanProcessor(exporter, scope)
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
        val processor = SimpleSpanProcessor(exporter, scope)

        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val exporter = FakeSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope)
        processor.shutdown()

        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testConcurrentExportsAreSerialized() = runTest {
        val exporter = SuspendingSpanExporter()
        val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
        val processor = SimpleSpanProcessor(exporter, scope)

        repeat(5) { processor.onEnd(FakeReadWriteSpan(name = "span_$it")) }
        advanceUntilIdle()

        assertFalse(exporter.observedConcurrentExport)
        assertEquals(List(5) { "span_$it" }, exporter.exports)
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
}
