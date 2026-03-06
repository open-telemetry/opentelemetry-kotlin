package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InMemorySpanExporterTest {

    private val fakeTelemetry = listOf(FakeReadWriteSpan())
    private lateinit var exporter: InMemorySpanExporter

    @BeforeTest
    @Suppress("DEPRECATION")
    fun setUp() {
        exporter = createInMemorySpanExporter()
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
        assertEquals(fakeTelemetry, exporter.exportedSpans)
    }

    @Test
    fun testExportReturnsFailureAfterShutdown() = runTest {
        exporter.shutdown()
        val result = exporter.export(fakeTelemetry)
        assertEquals(OperationResultCode.Failure, result)
        assertTrue(exporter.exportedSpans.isEmpty())
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
