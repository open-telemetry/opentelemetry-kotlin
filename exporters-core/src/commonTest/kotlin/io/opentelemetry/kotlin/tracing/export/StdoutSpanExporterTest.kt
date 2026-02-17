package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.framework.loadTestFixture
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.data.FakeEventData
import io.opentelemetry.kotlin.tracing.data.FakeLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class StdoutSpanExporterTest {

    private val exportConfig = FakeTraceExportConfig()

    @Test
    fun testExportMinimalSpan() = runTest {
        val output = mutableListOf<String>()
        val exporter = exportConfig.stdoutSpanExporter(output::add)

        val span = FakeReadWriteSpan(
            name = "test-span",
            endTimestamp = null,
            status = StatusData.Error("Whoops"),
            instrumentationScopeInfo = FakeInstrumentationScopeInfo("0.1.0", null, null, emptyMap())
        )

        val result = exporter.export(listOf(span))
        assertEquals(OperationResultCode.Success, result)
        assertEquals(1, output.size)

        val expected = loadTestFixture("stdout_span_output_minimal.txt")
        assertEquals(expected, output.single())
    }

    @Test
    fun testExportSpan() = runTest {
        val output = mutableListOf<String>()
        val exporter = exportConfig.stdoutSpanExporter(output::add)

        val span = FakeReadWriteSpan(
            name = "test-span",
            spanKind = SpanKind.SERVER,
            status = StatusData.Ok,
            spanContext = FakeSpanContext.VALID,
            parent = FakeSpanContext.INVALID,
            startTimestamp = 1000000000L,
            endTimestamp = 2000000000L,
            attributes = mapOf("http.method" to "GET", "http.status_code" to 200),
            events = listOf(
                FakeEventData(name = "request.started", timestamp = 1100000000L),
                FakeEventData(name = "request.completed", timestamp = 1900000000L)
            ),
            links = listOf(
                FakeLinkData()
            ),
            resource = FakeResource(attributes = mapOf("service.name" to "test-service")),
            instrumentationScopeInfo = FakeInstrumentationScopeInfo(
                name = "io.opentelemetry.test",
                version = "1.0.0"
            ),
            hasEnded = true
        )

        val result = exporter.export(listOf(span))
        assertEquals(OperationResultCode.Success, result)
        assertEquals(1, output.size)

        val expected = loadTestFixture("stdout_span_output.txt")
        assertEquals(expected, output.single())
    }

    @Test
    fun testForceFlush() = runTest {
        val exporter = StdoutSpanExporter()
        assertEquals(OperationResultCode.Success, exporter.forceFlush())
    }

    @Test
    fun testShutdown() = runTest {
        val exporter = StdoutSpanExporter()
        assertEquals(OperationResultCode.Success, exporter.shutdown())
    }
}
