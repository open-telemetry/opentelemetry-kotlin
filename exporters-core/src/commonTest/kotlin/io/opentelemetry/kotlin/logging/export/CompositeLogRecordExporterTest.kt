package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.logging.model.FakeReadableLogRecord
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompositeLogRecordExporterTest {

    private val fakeTelemetry = listOf(FakeReadableLogRecord())
    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        errorHandler = FakeSdkErrorHandler()
    }

    @Test
    fun testNoSpanExporters() {
        val exporter =
            CompositeLogRecordExporter(
                emptyList(),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Success,
            Success,
            Success
        )
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testMultipleSpanExporters() {
        val first = FakeLogRecordExporter()
        val second = FakeLogRecordExporter()
        val exporter =
            CompositeLogRecordExporter(
                listOf(first, second),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Success,
            Success,
            Success
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneExporterExportFails() {
        val first = FakeLogRecordExporter(action = { Failure })
        val second = FakeLogRecordExporter()
        val exporter =
            CompositeLogRecordExporter(
                listOf(first, second),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Failure,
            Success,
            Success
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneExporterFlushFails() {
        val first = FakeLogRecordExporter(flushCode = { Failure })
        val second = FakeLogRecordExporter()
        val exporter =
            CompositeLogRecordExporter(
                listOf(first, second),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Success,
            Failure,
            Success
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneExporterShutdownFails() {
        val first = FakeLogRecordExporter(shutdownCode = { Failure })
        val second = FakeLogRecordExporter()
        val exporter =
            CompositeLogRecordExporter(
                listOf(first, second),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Success,
            Success,
            Failure
        )
        assertTelemetryCapturedSuccess(first, second)
    }

    @Test
    fun testOneExporterThrowsInExport() {
        val first = FakeLogRecordExporter(action = { throw IllegalStateException() })
        val second = FakeLogRecordExporter()
        val exporter =
            CompositeLogRecordExporter(
                listOf(first, second),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Failure,
            Success,
            Success
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneExporterThrowsInFlush() {
        val first = FakeLogRecordExporter(flushCode = { throw IllegalStateException() })
        val second = FakeLogRecordExporter()
        val exporter =
            CompositeLogRecordExporter(
                listOf(first, second),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Success,
            Failure,
            Success
        )
        assertTelemetryCapturedFailure(first, second)
    }

    @Test
    fun testOneExporterThrowsInShutdown() {
        val first = FakeLogRecordExporter(shutdownCode = { throw IllegalStateException() })
        val second = FakeLogRecordExporter()
        val exporter =
            CompositeLogRecordExporter(
                listOf(first, second),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Success,
            Success,
            Failure
        )
        assertTelemetryCapturedFailure(first, second)
    }

    private fun CompositeLogRecordExporter.assertReturnValuesMatch(
        export: OperationResultCode,
        flush: OperationResultCode,
        shutdown: OperationResultCode
    ) {
        assertEquals(shutdown, shutdown())
        assertEquals(flush, forceFlush())
        assertEquals(export, export(fakeTelemetry))
    }

    private fun assertTelemetryCapturedSuccess(
        first: FakeLogRecordExporter,
        second: FakeLogRecordExporter
    ) {
        assertFalse(errorHandler.hasErrors())
        assertSame(fakeTelemetry.single(), first.logs.single())
        assertSame(fakeTelemetry.single(), second.logs.single())
    }

    private fun assertTelemetryCapturedFailure(
        first: FakeLogRecordExporter,
        second: FakeLogRecordExporter
    ) {
        assertTrue(errorHandler.hasErrors())
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertSame(fakeTelemetry.single(), first.logs.single())
        assertSame(fakeTelemetry.single(), second.logs.single())
    }
}
