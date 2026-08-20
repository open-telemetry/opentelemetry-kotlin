package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.SpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExportLogsServiceRequestTest {

    private val telemetry = FakeLogRecordData(
        eventName = "foo",
        spanContext = FakeSpanContext(
            traceIdBytes = "12345678901234567890123456789012".hexToByteArray(),
            spanIdBytes = "1234567890123456".hexToByteArray(),
            traceFlags = FakeTraceFlags(isSampled = true),
            traceState = FakeTraceState(emptyMap())
        )
    )

    @Test
    fun testProtobufToByteArray() {
        val original = listOf(telemetry)
        val byteArray = original.toProtobufByteArray()
        val result = byteArray.toLogRecordDataList()

        assertEquals(1, result.size)
        val expected = original[0]
        val record = result[0]
        assertEquals(expected.timestamp, record.timestamp)
        assertEquals(expected.observedTimestamp, record.observedTimestamp)
        assertEquals(expected.severityNumber, record.severityNumber)
        assertEquals(expected.severityText, record.severityText)
        assertEquals(expected.body, record.body)
        assertEquals(expected.attributes, record.attributes)
        assertEquals(expected.resource.attributes, record.resource.attributes)
        assertSpanContextMatches(expected.spanContext, record.spanContext)
        assertScopeMatches(
            expected.instrumentationScopeInfo,
            record.instrumentationScopeInfo
        )
    }

    @Test
    fun testAllSeverityNumbersRoundTrip() {
        val severityLevels = listOf(
            SeverityNumber.TRACE,
            SeverityNumber.TRACE2,
            SeverityNumber.TRACE3,
            SeverityNumber.TRACE4,
            SeverityNumber.DEBUG,
            SeverityNumber.DEBUG2,
            SeverityNumber.DEBUG3,
            SeverityNumber.DEBUG4,
            SeverityNumber.INFO,
            SeverityNumber.INFO2,
            SeverityNumber.INFO3,
            SeverityNumber.INFO4,
            SeverityNumber.WARN,
            SeverityNumber.WARN2,
            SeverityNumber.WARN3,
            SeverityNumber.WARN4,
            SeverityNumber.ERROR,
            SeverityNumber.ERROR2,
            SeverityNumber.ERROR3,
            SeverityNumber.ERROR4,
            SeverityNumber.FATAL,
            SeverityNumber.FATAL2,
            SeverityNumber.FATAL3,
            SeverityNumber.FATAL4
        )

        severityLevels.forEach { severity ->
            val record = FakeLogRecordData(severityNumber = severity)
            val byteArray = listOf(record).toProtobufByteArray()
            val result = byteArray.toLogRecordDataList()
            assertEquals(severity, result[0].severityNumber, "Failed for severity: $severity")
        }
    }

    @Test
    fun testNullRoundTrip() {
        val record = FakeLogRecordData(
            severityNumber = null,
            severityText = null,
            body = null,
            eventName = null,
        )
        val byteArray = listOf(record).toProtobufByteArray()
        val result = byteArray.toLogRecordDataList()
        assertNull(result[0].severityNumber)
        assertNull(result[0].severityText)
        assertNull(result[0].body)
        assertNull(result[0].eventName)
    }

    @Test
    fun testEventNameRoundTrip() {
        val record = FakeLogRecordData(eventName = "test-event")
        val byteArray = listOf(record).toProtobufByteArray()
        val result = byteArray.toLogRecordDataList()
        assertEquals("test-event", result[0].eventName)
    }

    private fun assertSpanContextMatches(
        expectedContext: SpanContext,
        actualContext: SpanContext
    ) {
        assertEquals(expectedContext.traceId, actualContext.traceId)
        assertEquals(expectedContext.spanId, actualContext.spanId)
        assertEquals(expectedContext.traceFlags.isSampled, actualContext.traceFlags.isSampled)
        assertEquals(expectedContext.traceFlags.isRandom, actualContext.traceFlags.isRandom)
        assertEquals(expectedContext.traceState.asMap(), actualContext.traceState.asMap())
    }

    private fun assertScopeMatches(
        expectedScope: InstrumentationScopeInfo,
        actualScope: InstrumentationScopeInfo
    ) {
        assertEquals(expectedScope.name, actualScope.name)
        assertEquals(expectedScope.version, actualScope.version)
        assertEquals(expectedScope.attributes, actualScope.attributes)
    }

    @Test
    fun testCreateExportLogsServiceRequest() {
        val request = listOf(telemetry).toExportLogsServiceRequest()
        assertEquals(1, request.resource_logs.size)
        val resourceLogs = request.resource_logs[0]

        assertEquals(1, resourceLogs.scope_logs.size)
        val scopeLogs = resourceLogs.scope_logs[0]

        assertEquals(1, scopeLogs.log_records.size)
        val logRecords = scopeLogs.log_records[0]

        assertEquals(telemetry.body, logRecords.body?.string_value)
    }

    @Test
    fun testGroupsLogsWithSameResourceAndScope() {
        val resource = FakeResource()
        val scope = FakeInstrumentationScopeInfo()
        val request = listOf(
            FakeLogRecordData(body = "a", resource = resource, instrumentationScopeInfo = scope),
            FakeLogRecordData(body = "b", resource = resource, instrumentationScopeInfo = scope),
        ).toExportLogsServiceRequest()

        assertEquals(1, request.resource_logs.size)
        assertEquals(1, request.resource_logs[0].scope_logs.size)
        assertEquals(2, request.resource_logs[0].scope_logs[0].log_records.size)
    }

    @Test
    fun testGroupsLogsWithSameResourceAndDifferentScopes() {
        val resource = FakeResource()
        val request = listOf(
            FakeLogRecordData(body = "a", resource = resource, instrumentationScopeInfo = FakeInstrumentationScopeInfo(name = "one")),
            FakeLogRecordData(body = "b", resource = resource, instrumentationScopeInfo = FakeInstrumentationScopeInfo(name = "two")),
        ).toExportLogsServiceRequest()

        assertEquals(1, request.resource_logs.size)
        assertEquals(2, request.resource_logs[0].scope_logs.size)
        assertEquals(1, request.resource_logs[0].scope_logs[0].log_records.size)
        assertEquals(1, request.resource_logs[0].scope_logs[1].log_records.size)
    }

    @Test
    fun testGroupsLogsWithDifferentResources() {
        val request = listOf(
            FakeLogRecordData(
                body = "a",
                resource = FakeResource(attributes = mapOf("service.name" to "a"), schemaUrl = "https://example.com/a"),
            ),
            FakeLogRecordData(
                body = "b",
                resource = FakeResource(attributes = mapOf("service.name" to "b"), schemaUrl = "https://example.com/b"),
            ),
        ).toExportLogsServiceRequest()

        assertEquals(2, request.resource_logs.size)
        assertEquals(1, request.resource_logs[0].scope_logs.size)
        assertEquals(1, request.resource_logs[1].scope_logs.size)
    }

    @Test
    fun testUsesEmptySchemaUrlWhenScopeHasNone() {
        val request = listOf(
            FakeLogRecordData(instrumentationScopeInfo = FakeInstrumentationScopeInfo(schemaUrl = null)),
        ).toExportLogsServiceRequest()

        assertEquals("", request.resource_logs[0].scope_logs[0].schema_url)
    }
}
