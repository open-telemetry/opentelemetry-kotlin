package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpFileLogRecordExporterTest {

    private val dispatcher = StandardTestDispatcher()

    private fun exporter() = OtlpFileLogRecordExporter(
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
        val logRecords = listOf(FakeLogRecordData())

        // when
        val result = exporter.export(logRecords)

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
        val logRecords = listOf(FakeLogRecordData())

        // when
        val code = exporter.export(logRecords)
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
