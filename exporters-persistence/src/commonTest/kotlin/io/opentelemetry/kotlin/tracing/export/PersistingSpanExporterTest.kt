package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.FakeTelemetryRepository
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class PersistingSpanExporterTest {

    private val telemetry = listOf<SpanData>(FakeSpanData(name = "test"))

    @Test
    fun testStoreCalledOnExport() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val exporter = PersistingSpanExporter(FakeSpanExporter(), repository)
        exporter.export(telemetry)

        assertEquals(1, repository.storeCalls)
        assertSame(telemetry, repository.storedTelemetry.last())
    }

    @Test
    fun testExportSuccess() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val exporter = PersistingSpanExporter(FakeSpanExporter(), repository)

        val result = exporter.export(telemetry)
        assertEquals(Success, result)
    }

    @Test
    fun testDelegateNotCalled() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val delegate = FakeSpanExporter()
        val exporter = PersistingSpanExporter(delegate, repository)

        exporter.export(telemetry)
        assertTrue(delegate.exports.isEmpty())
    }

    @Test
    fun testExportStillWorksIfStoreFails() = runTest {
        val repository = FakeTelemetryRepository<SpanData>(storeFails = true)
        val delegate = FakeSpanExporter()
        val exporter = PersistingSpanExporter(delegate, repository)

        val result = exporter.export(telemetry)
        assertEquals(Success, result)
        assertEquals("test", delegate.exports.single().name)
        assertEquals(0, repository.deleteCalls)
    }

    @Test
    fun testFallbackExport() = runTest {
        val repository = FakeTelemetryRepository<SpanData>(storeFails = true)
        val exporter = PersistingSpanExporter(
            FakeSpanExporter(exportReturnValue = { Failure }),
            repository,
        )

        val result = exporter.export(telemetry)
        assertEquals(Failure, result)
    }

    @Test
    fun testForceFlushReturnsSuccess() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val exporter = PersistingSpanExporter(FakeSpanExporter(), repository)
        assertEquals(Success, exporter.forceFlush())
    }

    @Test
    fun testShutdownReturnsSuccess() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val exporter = PersistingSpanExporter(FakeSpanExporter(), repository)
        assertEquals(Success, exporter.shutdown())
        assertEquals(Success, exporter.shutdown())
    }
}
