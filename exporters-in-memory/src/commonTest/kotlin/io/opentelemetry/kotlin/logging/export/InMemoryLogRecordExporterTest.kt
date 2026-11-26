package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InMemoryLogRecordExporterTest {

    private val fakeTelemetry = listOf(FakeLogRecordData())
    private lateinit var exporter: InMemoryLogRecordExporter

    @BeforeTest
    fun setUp() {
        exporter = InMemoryLogRecordExporterImpl()
    }

    @Test
    fun testExporterShutdown() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testExporterForceFlush() = runTest {
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testExport() = runTest {
        exporter.export(fakeTelemetry)
        assertEquals(fakeTelemetry, exporter.exportedLogRecords)
    }

    @Test
    fun testExportedRecordsAreSnapshots() = runTest {
        exporter.export(fakeTelemetry)
        val firstSnapshot = exporter.exportedLogRecords

        exporter.export(fakeTelemetry)

        assertEquals(fakeTelemetry, firstSnapshot)
        assertEquals(fakeTelemetry + fakeTelemetry, exporter.exportedLogRecords)
    }

    @Test
    fun testResetClearsTelemetryAndAllowsFurtherExports() = runTest {
        exporter.export(fakeTelemetry)
        val snapshot = exporter.exportedLogRecords

        exporter.reset()

        assertTrue(exporter.exportedLogRecords.isEmpty())
        assertEquals(fakeTelemetry, snapshot)
        assertEquals(OperationResultCode.Success, exporter.export(fakeTelemetry))
        assertEquals(fakeTelemetry, exporter.exportedLogRecords)
    }

    @Test
    fun testResetIsIdempotent() {
        exporter.reset()
        exporter.reset()
        assertTrue(exporter.exportedLogRecords.isEmpty())
    }

    @Test
    fun testResetAfterShutdownDoesNotReactivateExporter() = runTest {
        exporter.export(fakeTelemetry)
        exporter.shutdown()

        exporter.reset()

        assertTrue(exporter.exportedLogRecords.isEmpty())
        assertEquals(OperationResultCode.Failure, exporter.export(fakeTelemetry))
        assertTrue(exporter.exportedLogRecords.isEmpty())
    }

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest {
        exporter.shutdown()
        val result = exporter.export(fakeTelemetry)
        assertEquals(OperationResultCode.Failure, result)
        assertTrue(exporter.exportedLogRecords.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        assertEquals(OperationResultCode.Success, exporter.shutdown())
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }

    @Test
    fun testForceFlushWorksAfterShutdown() = runTest {
        exporter.shutdown()
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }
}
