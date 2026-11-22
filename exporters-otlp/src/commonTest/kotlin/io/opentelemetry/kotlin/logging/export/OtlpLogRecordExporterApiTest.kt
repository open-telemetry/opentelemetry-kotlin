package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.ExportConfigDsl
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpLogRecordExporterApiTest {

    @Test
    fun `should successfully create file logrecord exporter with dsl and export to a sink`() = runTest {
        // given
        val logRecords = listOf(FakeLogRecordData(), FakeLogRecordData())
        val fileLogRecordExporter = fakeConfig().otlpFileLogRecordExporter {
            sink = {}
        }

        // when
        val result = fileLogRecordExporter.export(logRecords)

        // then
        assertEquals(
            expected = OperationResultCode.Success,
            actual = result
        )
    }
}

private fun fakeConfig(): ExportConfigDsl = object : ExportConfigDsl {
    override val clock: Clock = FakeClock()
    override val sdkErrorHandler = NoopSdkErrorHandler
}
