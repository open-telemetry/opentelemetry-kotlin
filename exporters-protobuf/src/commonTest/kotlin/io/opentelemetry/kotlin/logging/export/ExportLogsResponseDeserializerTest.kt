package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.proto.collector.logs.v1.ExportLogsPartialSuccess
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ExportLogsResponseDeserializerTest {

    @Test
    fun testNoErrorMessage() {
        val success = ExportLogsServiceResponse(partial_success = null)
        val result =
            ExportLogsServiceResponse.ADAPTER.encode(success).deserializeLogRecordErrorMessage()
        assertNull(result)
    }

    @Test
    fun testWithErrorMessage() {
        val success = ExportLogsServiceResponse(partial_success = ExportLogsPartialSuccess(
            42L, "my_error"
        ))
        val result =
            ExportLogsServiceResponse.ADAPTER.encode(success).deserializeLogRecordErrorMessage()
        assertEquals("my_error", result)
    }
}