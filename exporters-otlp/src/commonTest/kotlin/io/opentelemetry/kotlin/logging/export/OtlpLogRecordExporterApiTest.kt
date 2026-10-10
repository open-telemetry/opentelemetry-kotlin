package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.LogExportConfigDsl
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import kotlinx.coroutines.test.runTest
import okio.blackholeSink
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpLogRecordExporterApiTest {

    @Test
    fun `should successfully create file logrecord exporter with dsl and export to a sink`() = runTest {
        // given
        val logRecords = listOf(FakeLogRecordData())
        val blackholeBufferSink = blackholeSink().buffer()
        val fileLogRecordExporter = fakeConfig().otlpFileLogRecordExporter {
            sink = blackholeBufferSink
        }

        // when
        val result = fileLogRecordExporter.export(logRecords)

        // then
        assertEquals(
            expected = OperationResultCode.Success,
            actual = result
        )
        assertEquals(
            expected = "",
            actual = blackholeBufferSink.buffer.readUtf8()
        )
    }
}

private fun fakeConfig(): LogExportConfigDsl = object : LogExportConfigDsl {
    override val clock: Clock = FakeClock()
    override val sdkErrorHandler = NoopSdkErrorHandler
}