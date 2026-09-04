package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JsonLogRecordExporterApiTest {

    private val config = FakeLogExportConfig()
    private val fakeLogRecord = FakeReadWriteLogRecord()

    @Test
    fun `should successfully create log record exporter force flush and shutdown`() = runTest {
        config.jsonLogRecordExporter().apply {
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
    }

    @Test
    fun `should successfully export log records force flush and shutdown`() = runTest {
        config.jsonLogRecordExporter().apply {
            assertEquals(
                OperationResultCode.Success,
                export(listOf(fakeLogRecord))
            )
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
    }
}