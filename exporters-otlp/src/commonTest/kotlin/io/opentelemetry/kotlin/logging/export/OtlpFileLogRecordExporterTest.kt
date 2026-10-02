package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpFileLogRecordExporterTest {

    @Test
    fun testExportInitialSuccess() = runTest {
        // given
        val exporter = OtlpFileLogRecordExporter(
            initialDelayMs = 3,
            maxAttemptIntervalMs = 5,
            maxAttempts = 3,
            sdkErrorHandler = NoopSdkErrorHandler,
        )
        val logRecords = listOf(FakeLogRecordData())

        // when
        val result = exporter.export(logRecords)

        // then
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testExportForceFlush() = runTest {
        // given
        val exporter = OtlpFileLogRecordExporter(
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
        val exporter = OtlpFileLogRecordExporter(
            initialDelayMs = 3,
            maxAttemptIntervalMs = 5,
            maxAttempts = 3,
            sdkErrorHandler = NoopSdkErrorHandler,
        )
        val logRecords = listOf(FakeLogRecordData())

        // when
        val code = exporter.export(logRecords)
        val shutdownCode = exporter.shutdown()

        // then
        assertEquals(OperationResultCode.Success, code)
        assertEquals(OperationResultCode.Success, shutdownCode)
    }

    @Test
    fun testExportNoOpOnEmpty() = runTest {
        // given
        val exporter = OtlpFileLogRecordExporter(
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
