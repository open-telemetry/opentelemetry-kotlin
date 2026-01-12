package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
class ExportLogsServiceRequestTest {

    @Test
    fun testProtobufToByteArray() {
        val byteArray = listOf(FakeReadableLogRecord()).toProtobufByteArray()
        val result = ExportLogsServiceRequest.ADAPTER.decode(byteArray)
        assertEquals(1,result.resource_logs.size)
    }

    @Test
    fun testCreateExportLogsServiceRequest() {
        val record = FakeReadableLogRecord()
        val request = listOf(record).toExportLogsServiceRequest()
        assertEquals(1, request.resource_logs.size)
        val resourceLogs = request.resource_logs[0]

        assertEquals(1, resourceLogs.scope_logs.size)
        val scopeLogs = resourceLogs.scope_logs[0]

        assertEquals(1, scopeLogs.log_records.size)
        val logRecords = scopeLogs.log_records[0]

        assertEquals(record.body, logRecords.body?.string_value)
    }
}