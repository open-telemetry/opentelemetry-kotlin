package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordData
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordExporter
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

internal class LogRecordExporterExtTest {

    @Test
    fun toOtelKotlinLogRecordExporter() = runTest {
        val impl = FakeOtelJavaLogRecordExporter()
        val adapter = impl.toOtelKotlinLogRecordExporter()
        val timestamp = 500L
        adapter.export(mutableListOf(FakeReadWriteLogRecord(timestamp = timestamp)))

        val export = impl.exports.single()
        assertEquals(timestamp, export.timestampEpochNanos)
    }

    @Test
    fun toOtelJavaLogRecordExporter() {
        val impl = FakeLogRecordExporter()
        val logs = mutableListOf<OtelJavaLogRecordData>(FakeOtelJavaLogRecordData(implTimestamp = 500L))

        val result = impl.toOtelJavaLogRecordExporter()
            .export(logs)
            .join(5, TimeUnit.SECONDS)

        assertEquals(true, result.isSuccess)
        assertEquals(500L, impl.logs.single().timestamp)
    }
}
