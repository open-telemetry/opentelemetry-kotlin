package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.FakeTelemetryRepository
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class PersistingLogRecordExporterTest {

    private val telemetry = listOf(FakeReadableLogRecord(body = "test"))

    @Test
    fun testStoreCalledOnExport() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>()
        val exporter = PersistingLogRecordExporter(FakeLogRecordExporter(), repository)
        exporter.export(telemetry)

        assertEquals(1, repository.storeCalls)
        assertSame(telemetry, repository.storedTelemetry.last())
    }

    @Test
    fun testExportReturnsSuccessWhenStoreSucceeds() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>()
        val exporter = PersistingLogRecordExporter(FakeLogRecordExporter(), repository)

        val result = exporter.export(telemetry)
        assertEquals(Success, result)
    }

    @Test
    fun testDelegateNotCalledWhenStoreSucceeds() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>()
        val delegate = FakeLogRecordExporter()
        val exporter = PersistingLogRecordExporter(delegate, repository)

        exporter.export(telemetry)
        assertTrue(delegate.logs.isEmpty())
    }

    @Test
    fun testExportStillWorksIfStoreFails() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>(storeFails = true)
        val delegate = FakeLogRecordExporter()
        val exporter = PersistingLogRecordExporter(delegate, repository)

        val result = exporter.export(telemetry)
        assertEquals(Success, result)
        assertEquals("test", delegate.logs.single().body)
        assertEquals(0, repository.deleteCalls)
    }

    @Test
    fun testFallbackExportResultPropagatedWhenStoreFails() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>(storeFails = true)
        val exporter = PersistingLogRecordExporter(
            FakeLogRecordExporter(action = { Failure }),
            repository,
        )

        val result = exporter.export(telemetry)
        assertEquals(Failure, result)
    }

    @Test
    fun testForceFlushReturnsSuccess() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>()
        val exporter = PersistingLogRecordExporter(FakeLogRecordExporter(), repository)
        assertEquals(Success, exporter.forceFlush())
    }

    @Test
    fun testShutdownReturnsSuccess() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>()
        val exporter = PersistingLogRecordExporter(FakeLogRecordExporter(), repository)
        assertEquals(Success, exporter.shutdown())
        assertEquals(Success, exporter.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        val repository = FakeTelemetryRepository<ReadableLogRecord>()
        val exporter = PersistingLogRecordExporter(FakeLogRecordExporter(), repository)
        exporter.shutdown()
        assertEquals(Success, exporter.forceFlush())
    }
}
