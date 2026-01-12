package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ExportTraceResponseDeserializerTest {

    @Test
    fun testNoErrorMessage() {
        val success = ExportTraceServiceResponse(partial_success = null)
        val result =
            ExportTraceServiceResponse.ADAPTER.encode(success).deserializeTraceRecordErrorMessage()
        assertNull(result)
    }

    @Test
    fun testWithErrorMessage() {
        val success = ExportTraceServiceResponse(
            partial_success = ExportTracePartialSuccess(
                42L, "my_error"
            )
        )
        val result =
            ExportTraceServiceResponse.ADAPTER.encode(success).deserializeTraceRecordErrorMessage()
        assertEquals("my_error", result)
    }
}