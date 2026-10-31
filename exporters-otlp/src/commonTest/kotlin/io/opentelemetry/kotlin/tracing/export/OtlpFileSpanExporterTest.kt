package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpFileSpanExporterTest {

    private val dispatcher = StandardTestDispatcher()

    private fun exporter() = OtlpFileSpanExporter(
        initialDelayMs = 3,
        maxAttemptIntervalMs = 5,
        maxAttempts = 3,
        sdkErrorHandler = NoopSdkErrorHandler,
        coroutineContext = dispatcher
    )

    @Test
    fun testExportInitialSuccess() = runTest(dispatcher) {
        // given
        val exporter = exporter()
        val spans = listOf(FakeSpanData())

        // when
        val result = exporter.export(spans)

        // then
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testExportForceFlush() = runTest(dispatcher) {
        // given
        val exporter = exporter()

        // when
        val result = exporter.forceFlush()

        // then
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testExportShutdown() = runTest(dispatcher) {
        // given
        val exporter = exporter()
        val spans = listOf(FakeSpanData())

        // when
        val code = exporter.export(spans)
        val shutdownCode = exporter.shutdown()

        // then
        assertEquals(OperationResultCode.Success, code)
        assertEquals(OperationResultCode.Success, shutdownCode)
    }

    @Test
    fun testExportNoOpOnEmpty() = runTest(dispatcher) {
        // given
        val exporter = exporter()

        // when
        val result = exporter.export(emptyList())

        // then
        assertEquals(OperationResultCode.Success, result)
    }
}
