package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.telemetryExceptionHandler
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
            FakeSdkErrorHandler(),
        )

        val log = FakeReadWriteLogRecord(body = "my_log")
        processor.onEmit(log, FakeContext())

        val export = exporter.logs.single()
        assertEquals(log.body, export.body)
    }

    @Test
    fun testExportReceivesImmutableSnapshot() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            FakeSdkErrorHandler(),
        )

        val log = FakeReadWriteLogRecord(body = "my_log")
        processor.onEmit(log, FakeContext())

        // the exporter retains plain data rather than the live log record
        val export = exporter.logs.single()
        assertFalse(export === log)

        log.body = "changed"
        assertEquals("my_log", export.body)
    }

    @Test
    fun testOnEmitNoOpAfterShutdown() = runTest {
        val exporter = FakeLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            FakeSdkErrorHandler(),
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
            FakeSdkErrorHandler(),
        )

        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testEnabledReturnsFalseAfterShutdown() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            FakeSdkErrorHandler(),
        )
        processor.shutdown()

        assertFalse(processor.enabled(FakeContext(), FakeInstrumentationScopeInfo(), null, null))
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val processor = SimpleLogRecordProcessor(
            FakeLogRecordExporter(),
            CoroutineScope(UnconfinedTestDispatcher(testScheduler)),
            FakeSdkErrorHandler(),
        )
        processor.shutdown()

        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testConcurrentExportsAreSerialized() = runTest {
        val exporter = SuspendingLogRecordExporter()
        val processor = SimpleLogRecordProcessor(
            exporter,
            CoroutineScope(StandardTestDispatcher(testScheduler)),
            FakeSdkErrorHandler(),
        )
        repeat(5) { processor.onEmit(FakeReadWriteLogRecord(body = "log_$it"), FakeContext()) }
        advanceUntilIdle()

        assertFalse(exporter.observedConcurrentExport)
        assertEquals(List(5) { "log_$it" }, exporter.exports)
    }

    @Test
    fun testExportFailureIsContained() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        var exportCount = 0
        val exporter = FakeLogRecordExporter(action = {
            if (exportCount++ == 0) {
                error("export failed")
            }
            OperationResultCode.Success
        })
        val scope = CoroutineScope(
            SupervisorJob() +
                UnconfinedTestDispatcher(testScheduler) +
                telemetryExceptionHandler("test", errorHandler)
        )
        val processor = SimpleLogRecordProcessor(exporter, scope, errorHandler)
        processor.onEmit(FakeReadWriteLogRecord(body = "first"), FakeContext())
        processor.onEmit(FakeReadWriteLogRecord(body = "second"), FakeContext())

        assertEquals(listOf("first", "second"), exporter.logs.map { it.body.toString() })
        assertEquals("export failed", errorHandler.userCodeErrors.single().cause.message)
        assertTrue(errorHandler.sdkCodeErrors.isEmpty())
    }

    @Test
    fun testForceFlushFailureIsContained() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FakeLogRecordExporter(flushCode = { error("flush failed") })
        val processor = SimpleLogRecordProcessor(exporter, this, errorHandler)

        assertEquals(OperationResultCode.Failure, processor.forceFlush())
        assertEquals("flush failed", errorHandler.userCodeErrors.single().cause.message)
        assertTrue(errorHandler.sdkCodeErrors.isEmpty())
    }

    @Test
    fun testShutdownFailureIsContained() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FakeLogRecordExporter(shutdownCode = { error("shutdown failed") })
        val processor = SimpleLogRecordProcessor(exporter, this, errorHandler)

        assertEquals(OperationResultCode.Failure, processor.shutdown())
        assertEquals("shutdown failed", errorHandler.userCodeErrors.single().cause.message)
        assertTrue(errorHandler.sdkCodeErrors.isEmpty())
    }

    private class SuspendingLogRecordExporter : LogRecordExporter {
        val exports: MutableList<String> = mutableListOf()
        var observedConcurrentExport: Boolean = false
        private var activeExports: Int = 0

        override suspend fun export(telemetry: List<LogRecordData>): OperationResultCode {
            activeExports++
            if (activeExports > 1) {
                observedConcurrentExport = true
            }
            delay(EXPORT_DURATION_MS)
            exports.addAll(telemetry.map { it.body.toString() })
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
