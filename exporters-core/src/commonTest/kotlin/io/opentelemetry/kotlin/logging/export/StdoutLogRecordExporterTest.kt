package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.loadTestFixture
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class StdoutLogRecordExporterTest {

    @Test
    fun testExportLogRecordMinimal() = runTest {
        val output = mutableListOf<String>()
        val exporter = FakeLogExportConfig().stdoutLogRecordExporter {
            output.add(it)
        }

        val logRecord = FakeLogRecordData(
            body = null,
            timestamp = null,
            observedTimestamp = null,
            severityNumber = null,
            severityText = null,
            attributes = emptyMap(),
            resource = FakeResource(emptyMap()),
            instrumentationScopeInfo = FakeInstrumentationScopeInfo("0.1.0", null, null, emptyMap())
        )

        val result = exporter.export(listOf(logRecord))
        assertEquals(OperationResultCode.Success, result)
        assertEquals(1, output.size)

        val expected = loadTestFixture("stdout_log_record_output_minimal.txt")
        assertEquals(expected, output.single())
    }

    @Test
    fun testExportLogRecord() = runTest {
        val output = mutableListOf<String>()
        val exporter = FakeLogExportConfig().stdoutLogRecordExporter {
            output.add(it)
        }

        val logRecord = FakeLogRecordData(
            timestamp = 1000000000L,
            observedTimestamp = 1000000100L,
            severityNumber = SeverityNumber.INFO,
            severityText = "INFO",
            body = "Application started successfully",
            eventName = "my_event",
            attributes = mapOf("thread.name" to "main", "code.function" to "start"),
            spanContext = FakeSpanContext.VALID,
            resource = FakeResource(attributes = mapOf("service.name" to "test-service")),
            instrumentationScopeInfo = FakeInstrumentationScopeInfo(
                name = "io.opentelemetry.test",
                version = "1.0.0"
            )
        )

        val result = exporter.export(listOf(logRecord))
        assertEquals(OperationResultCode.Success, result)
        assertEquals(1, output.size)

        val expected = loadTestFixture("stdout_log_record_output.txt")
        assertEquals(expected, output.single())
    }

    @Test
    fun testForceFlush() = runTest {
        val exporter = StdoutLogRecordExporter(NoopSdkErrorHandler)
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testShutdown() = runTest {
        val exporter = StdoutLogRecordExporter(NoopSdkErrorHandler)
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest {
        val output = mutableListOf<String>()
        val exporter = StdoutLogRecordExporter(NoopSdkErrorHandler, output::add)
        exporter.shutdown()

        val logRecord = FakeLogRecordData()
        val result = exporter.export(listOf(logRecord))
        assertEquals(OperationResultCode.Failure, result)
        assertEquals(0, output.size)
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        val exporter = StdoutLogRecordExporter(NoopSdkErrorHandler)
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val exporter = StdoutLogRecordExporter(NoopSdkErrorHandler)
        exporter.shutdown()
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testItemThatFailsToFormatDoesNotStopTheBatch() = runTest {
        val output = mutableListOf<String>()
        val errorHandler = FakeSdkErrorHandler()
        val exporter = FakeLogExportConfig(sdkErrorHandler = errorHandler).stdoutLogRecordExporter(output::add)
        val throwingLogRecord = object : LogRecordData by FakeLogRecordData() {
            override val severityText: String get() = error("boom")
        }

        val result = exporter.export(
            listOf(FakeLogRecordData(body = "first"), throwingLogRecord, FakeLogRecordData(body = "third"))
        )

        assertEquals(OperationResultCode.Failure, result)
        assertEquals(2, output.size)
        assertTrue(output[0].contains("Body: first"))
        assertTrue(output[1].contains("Body: third"))
        assertEquals(1, errorHandler.errors.size)
        assertEquals(1, errorHandler.userCodeErrors.size)
    }
}
