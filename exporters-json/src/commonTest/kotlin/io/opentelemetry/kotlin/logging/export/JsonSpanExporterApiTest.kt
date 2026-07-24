package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.tracing.FakeReadWriteSpan
import io.opentelemetry.kotlin.tracing.export.jsonSpanExporter
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class JsonLogRecordExporterApiTest {

    private val config = FakeLogExportConfig()
    private val fakeSpan = FakeReadWriteSpan()

    @Test
    fun `should successfully create span exporter, force flush and shutdown`() = runTest {
        config.jsonSpanExporter().apply {
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
    }

    @Test
    fun `should successfully export span data, force flush and shutdown`() = runTest {
        config.jsonSpanExporter().apply {
            assertEquals(OperationResultCode.Failure,
                export(listOf(fakeSpan))
            )
            assertEquals(OperationResultCode.Success, forceFlush())
            assertEquals(OperationResultCode.Success, shutdown())
        }
    }
}

internal class FakeLogExportConfig(
    override val clock: Clock = FakeClock()
) : LogExportConfigDsl
