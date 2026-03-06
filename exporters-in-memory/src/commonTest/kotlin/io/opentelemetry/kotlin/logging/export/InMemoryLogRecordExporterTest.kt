package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InMemoryLogRecordExporterTest {

    private val fakeTelemetry = listOf(FakeReadableLogRecord())
    private lateinit var exporter: InMemoryLogRecordExporter

    @BeforeTest
    @Suppress("DEPRECATION")
    fun setUp() {
        exporter = createInMemoryLogRecordExporter()
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
