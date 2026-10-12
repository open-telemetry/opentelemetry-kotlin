package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpFileSpanExporterTest {

    @Test
    fun testExportInitialSuccess() = runTest {
        // given
        val exporter = OtlpFileSpanExporter(
            initialDelayMs = 3,
            maxAttemptIntervalMs = 5,
            maxAttempts = 3,
            sdkErrorHandler = NoopSdkErrorHandler
        )
        val spans = listOf(FakeSpanData())

        // when
        val result = exporter.export(spans)

        // then
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testExportForceFlush() = runTest {
        // given
        val exporter = OtlpFileSpanExporter(
            initialDelayMs = 3,
            maxAttemptIntervalMs = 5,
            maxAttempts = 3,
            sdkErrorHandler = NoopSdkErrorHandler,
        )

        // when
        val result = exporter.forceFlush()

        // then
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testExportShutdown() = runTest {
        // given
        val exporter = OtlpFileSpanExporter(
            initialDelayMs = 3,
            maxAttemptIntervalMs = 5,
            maxAttempts = 3,
            sdkErrorHandler = NoopSdkErrorHandler,
        )
        val spans = listOf(FakeSpanData())

        // when
        val code = exporter.export(spans)
        val shutdownCode = exporter.shutdown()

        // then
        assertEquals(OperationResultCode.Success, code)
        assertEquals(OperationResultCode.Success, shutdownCode)
    }

    @Test
    fun testExportNoOpOnEmpty() = runTest {
        // given
        val exporter = OtlpFileSpanExporter(
            initialDelayMs = 3,
            maxAttemptIntervalMs = 5,
            maxAttempts = 3,
            sdkErrorHandler = NoopSdkErrorHandler,
        )

        // when
        val result = exporter.export(emptyList())

        // then
        assertEquals(OperationResultCode.Success, result)
    }
}
