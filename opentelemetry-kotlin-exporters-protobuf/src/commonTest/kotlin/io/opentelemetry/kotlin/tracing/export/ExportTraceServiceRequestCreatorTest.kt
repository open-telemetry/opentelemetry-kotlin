package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.toHexString
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
class ExportTraceServiceRequestCreatorTest {

    @Test
    fun testProtobufToByteArray() {
        val byteArray = listOf(FakeSpanData()).toProtobufByteArray()
        val result = ExportTraceServiceRequest.ADAPTER.decode(byteArray)
        assertEquals(1,result.resource_spans.size)
    }

    @Test
    fun testCreateExportTraceServiceRequest() {
        val spanData = FakeSpanData()
        val request = listOf(spanData).toExportTraceServiceRequest()
        assertEquals(1, request.resource_spans.size)
        val resourceSpans = request.resource_spans[0]
        assertEquals(1, resourceSpans.scope_spans.size)

        val scopeSpans = resourceSpans.scope_spans[0]
        assertEquals(1, scopeSpans.spans.size)
        val span = scopeSpans.spans[0]
        assertEquals(spanData.spanContext.traceId, span.trace_id.toByteArray().toHexString())
    }
}