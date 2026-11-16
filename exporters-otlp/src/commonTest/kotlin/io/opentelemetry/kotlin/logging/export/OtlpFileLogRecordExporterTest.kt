package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.logging.data.LogRecordData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OtlpFileLogRecordExporterTest {

    private val dispatcher = StandardTestDispatcher()
    private val errorHandler = FakeSdkErrorHandler()

    private fun exporter() = OtlpFileLogRecordExporter(
        initialDelayMs = 3,
        maxAttemptIntervalMs = 5,
        maxAttempts = 3,
        sdkErrorHandler = errorHandler,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testEncodingFailureIsReportedOnceAndNotRetried() = runTest(dispatcher) {
        val exporter = exporter()
        var encodeAttempts = 0
        val throwing = object : LogRecordData by FakeLogRecordData() {
            override val severityText: String
                get() {
                    encodeAttempts++
                    error("boom")
                }
        }

        exporter.export(listOf(throwing))
        advanceUntilIdle()
        exporter.shutdown()

        assertEquals(1, encodeAttempts)
        assertEquals(1, errorHandler.errors.size)
        assertEquals(1, errorHandler.sdkCodeErrors.size)
    }
}
