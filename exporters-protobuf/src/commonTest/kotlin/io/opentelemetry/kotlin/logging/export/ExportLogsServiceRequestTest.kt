package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.model.SpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
class ExportLogsServiceRequestTest {

    private val telemetry = FakeReadableLogRecord(
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
        val result = byteArray.toReadableLogRecordList()

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
            val record = FakeReadableLogRecord(severityNumber = severity)
            val byteArray = listOf(record).toProtobufByteArray()
            val result = byteArray.toReadableLogRecordList()
            assertEquals(severity, result[0].severityNumber, "Failed for severity: $severity")
        }
    }

    @Test
    fun testNullRoundTrip() {
        val record = FakeReadableLogRecord(
            severityNumber = null,
            severityText = null,
            body = null,
            eventName = null,
        )
        val byteArray = listOf(record).toProtobufByteArray()
        val result = byteArray.toReadableLogRecordList()
        assertNull(result[0].severityNumber)
        assertNull(result[0].severityText)
        assertNull(result[0].body)
        assertNull(result[0].eventName)
    }

    @Test
    fun testEventNameRoundTrip() {
        val record = FakeReadableLogRecord(eventName = "test-event")
        val byteArray = listOf(record).toProtobufByteArray()
        val result = byteArray.toReadableLogRecordList()
        assertEquals("test-event", result[0].eventName)
    }

    private fun assertSpanContextMatches(
        expectedContext: FakeSpanContext,
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
}
