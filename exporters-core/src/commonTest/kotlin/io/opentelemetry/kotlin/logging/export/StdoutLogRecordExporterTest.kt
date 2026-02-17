package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.loadTestFixture
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class StdoutLogRecordExporterTest {

    @Test
    fun testExportLogRecordMinimal() = runTest {
        val output = mutableListOf<String>()
        val exporter = FakeLogExportConfig().stdoutLogRecordExporter {
            output.add(it)
        }

        val logRecord = FakeReadableLogRecord(
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

        val logRecord = FakeReadableLogRecord(
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
        val exporter = StdoutLogRecordExporter()
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testShutdown() = runTest {
        val exporter = StdoutLogRecordExporter()
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }
}
