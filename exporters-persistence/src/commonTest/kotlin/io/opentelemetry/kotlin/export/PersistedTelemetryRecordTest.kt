package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.export.PersistedTelemetryRecord.Companion.comparator
import io.opentelemetry.kotlin.export.PersistedTelemetryRecord.Companion.fromFilename
import io.opentelemetry.kotlin.export.PersistedTelemetryType.LOGS
import io.opentelemetry.kotlin.export.PersistedTelemetryType.SPANS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PersistedTelemetryRecordTest {

    @Test
    fun testFilenameEncodesLogType() {
        val record = PersistedTelemetryRecord(
            timestamp = 1234567890L,
            type = LOGS,
            uid = "abc-123"
        )
        assertEquals("LOGS_1234567890_abc-123.gz", record.filename)
        val decoded = fromFilename(record.filename)
        assertEquals(record, decoded)
    }

    @Test
    fun testFilenameEncodesSpansType() {
        val record = PersistedTelemetryRecord(
            timestamp = 9876543210L,
            type = SPANS,
            uid = "xyz-789"
        )
        assertEquals("SPANS_9876543210_xyz-789.gz", record.filename)
        val decoded = fromFilename(record.filename)
        assertEquals(record, decoded)
    }

    @Test
    fun testFromFilenameReturnsNullForInvalid() {
        val invalid = listOf(
            "LOGS_1234567890_abc-123",
            "INVALID_1234567890_abc-123.gz",
            "LOGS_notanumber_abc-123.gz",
            "LOGS_1234567890.gz",
            "",
        )
        invalid.forEach {
            val record = fromFilename(it)
            assertNull(record)
        }
    }

    @Test
    fun testFromFilenamePreservesuidWithUnderscores() {
        val record = fromFilename("LOGS_1234567890_uid_with_underscores.gz")
        assertEquals("uid_with_underscores", record?.uid)
    }

    @Test
    fun testComparatorSorting() {
        val a = PersistedTelemetryRecord(200L, SPANS, "a")
        val b = PersistedTelemetryRecord(100L, LOGS, "b")
        val c = PersistedTelemetryRecord(100L, LOGS, "a")
        val d = PersistedTelemetryRecord(100L, SPANS, "a")
        val e = PersistedTelemetryRecord(200L, LOGS, "a")

        val records = listOf(a, b, c, d, e)
        val sorted = records.sortedWith(comparator)
        // Sorted by timestamp (oldest first), then type, then uid
        assertEquals(listOf(c, b, d, e, a), sorted)
    }

    @Test
    fun testSortEmptyList() {
        val sorted = emptyList<PersistedTelemetryRecord>().sortedWith(comparator)
        assertEquals(emptyList(), sorted)
    }

    @Test
    fun testSortSingleElement() {
        val record = PersistedTelemetryRecord(100L, LOGS, "a")
        val sorted = listOf(record).sortedWith(comparator)
        assertEquals(listOf(record), sorted)
    }
}
