package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SimpleSpanProcessorTest {

    @Test
    fun testSpanProcessorDefaults() = runTest {
        val processor = FakeTraceExportConfig().simpleSpanProcessor(FakeSpanExporter())
        assertTrue(processor.isStartRequired())
        assertTrue(processor.isEndRequired())
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
}
