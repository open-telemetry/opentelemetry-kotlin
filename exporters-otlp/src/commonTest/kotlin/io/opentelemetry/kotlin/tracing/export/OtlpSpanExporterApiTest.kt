package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.TraceExportConfigDsl
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.test.runTest
import okio.blackholeSink
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpSpanExporterApiTest {

    @Test
    fun `should successfully create file span exporter with dsl and export to a sink`() = runTest {
        // given
        val spans = listOf(FakeSpanData())
        val blackholeBufferSink = blackholeSink().buffer()
        val fileSpanExporter = fakeConfig().otlpFileSpanExporter {
            sink = blackholeBufferSink
        }

        // when
        val result = fileSpanExporter.export(spans)

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

private fun fakeConfig(): TraceExportConfigDsl = object : TraceExportConfigDsl {
    override val clock: Clock = FakeClock()
    override val sdkErrorHandler = NoopSdkErrorHandler
}