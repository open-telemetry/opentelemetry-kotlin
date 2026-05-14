package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame

internal class CoreLogRecordExporterApiTest {

    private val config = FakeLogExportConfig()
    private val fakeLogRecord = FakeReadWriteLogRecord()
    private val fakeContext = FakeContext()
    private val scopeInfo = FakeInstrumentationScopeInfo()

    @Test
    fun compositeLogRecordProcessorEmptyReturnsNoopWithoutThrowing() = runTest {
        val emptyProcessor = config.compositeLogRecordProcessor().apply {
            onEmit(fakeLogRecord, fakeContext)
            assertFalse(enabled(fakeContext, scopeInfo, null, null))
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }

        assertSame(NoopLogRecordProcessor, emptyProcessor)
    }

    @Test
    fun compositeLogRecordExporterEmptyReturnsNoopWithoutThrowing() = runTest {
        val emptyExporter = config.compositeLogRecordExporter().apply {
            assertEquals(OperationResultCode.Failure, export(listOf(fakeLogRecord)))
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
        assertSame(NoopLogRecordExporter, emptyExporter)
    }
}
