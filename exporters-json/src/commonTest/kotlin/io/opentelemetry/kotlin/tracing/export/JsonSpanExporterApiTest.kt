package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.export.FakeTraceExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JsonSpanExporterApiTest {

    private val config = FakeTraceExportConfig()
    private val fakeSpan = FakeReadWriteSpan()

    @Test
    fun `should successfully create span exporter force flush and shutdown`() = runTest {
        config.jsonSpanExporter().apply {
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
    }

    @Test
    fun `should successfully export span data force flush and shutdown`() = runTest {
        config.jsonSpanExporter().apply {
            assertEquals(
                OperationResultCode.Success,
                export(listOf(fakeSpan))
            )
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
    }
}
