package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class ReadableLogRecordExtTest {

    @Test
    fun testLogRecordDefaultConversions() {
        val record = FakeReadableLogRecord()
        val observed = record.toLogRecordData()
        assertEquals(record.timestamp, observed.timestampEpochNanos)
        assertEquals(record.observedTimestamp, observed.observedTimestampEpochNanos)
        assertEquals(record.severityText, observed.severityText)
        assertEquals(OtelJavaSeverity.WARN, observed.severity)
        assertEquals(record.body, observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordNullConversions() {
        val record = FakeReadableLogRecord(
            timestamp = null,
            observedTimestamp = null,
            severityNumber = null,
            severityText = null,
            body = null,
        )
        val observed = record.toLogRecordData()
        assertEquals(0, observed.timestampEpochNanos)
        assertEquals(0, observed.observedTimestampEpochNanos)
        assertEquals(OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER, observed.severity)
        assertNull(observed.severityText)
        assertNull(observed.bodyValue?.asString())
    }
}
