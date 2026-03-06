package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, ExperimentalCoroutinesApi::class)
internal class SimpleLogRecordProcessorTest {

    @Test
    fun testSpanProcessorDefaults() = runTest {
        val processor = FakeLogExportConfig().simpleLogRecordProcessor(FakeLogRecordExporter())
        assertTrue(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testSpanProcessorExport() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        val log = FakeReadWriteLogRecord(body = "my_log")
        processor.onEmit(log, FakeContext())

        val export = exporter.logs.single()
        assertEquals(log.body, export.body)
    }

    @Test
    fun testOnEmitNoOpAfterShutdown() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        processor.shutdown()

        val log = FakeReadWriteLogRecord()
        processor.onEmit(log, FakeContext())
        assertTrue(exporter.logs.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )

        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testEnabledReturnsFalseAfterShutdown() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        processor.shutdown()

        assertFalse(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
        )
        processor.shutdown()

        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }
}
