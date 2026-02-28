package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.FakeTelemetryRepository
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.data.SpanData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
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
    fun testDeleteCalledOnSuccess() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val exporter = PersistingSpanExporter(
            FakeSpanExporter(exportReturnValue = { Success }),
            repository,
        )

        exporter.export(telemetry)
        assertEquals(1, repository.deleteCalls)
    }

    @Test
    fun testDeleteNotCalledOnFailure() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val exporter = PersistingSpanExporter(
            FakeSpanExporter(exportReturnValue = { Failure }),
            repository,
        )

        exporter.export(telemetry)
        assertEquals(0, repository.deleteCalls)
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
    fun testExportResultPropagated() = runTest {
        val repository = FakeTelemetryRepository<SpanData>()
        val exporter = PersistingSpanExporter(
            FakeSpanExporter(exportReturnValue = { Failure }),
            repository,
        )

        val result = exporter.export(telemetry)
        assertEquals(Failure, result)
    }
}
