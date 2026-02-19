package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
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
}
