package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class InMemorySpanExporterTest {

    private val fakeTelemetry = listOf(FakeReadWriteSpan())
    private lateinit var exporter: InMemorySpanExporter

    @BeforeTest
    fun setUp() {
        exporter = InMemorySpanExporterImpl()
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
}
