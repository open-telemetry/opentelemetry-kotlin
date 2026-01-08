package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.export.OperationResultCode.Failure
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompositeSpanExporterTest {

    private val fakeTelemetry = listOf(FakeSpanData())
    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        errorHandler = FakeSdkErrorHandler()
    }

    @Test
    fun testNoSpanExporters() {
        val exporter =
            CompositeSpanExporter(
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
        val first = FakeSpanExporter()
        val second = FakeSpanExporter()
        val exporter =
            CompositeSpanExporter(
                listOf(
                    first,
                    second
                ),
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
        val first = FakeSpanExporter(exportReturnValue = { Failure })
        val second = FakeSpanExporter()
        val exporter =
            CompositeSpanExporter(
                listOf(
                    first,
                    second
                ),
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
        val first = FakeSpanExporter(forceFlushReturnValue = { Failure })
        val second = FakeSpanExporter()
        val exporter =
            CompositeSpanExporter(
                listOf(
                    first,
                    second
                ),
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
        val first = FakeSpanExporter(shutdownReturnValue = { Failure })
        val second = FakeSpanExporter()
        val exporter =
            CompositeSpanExporter(
                listOf(
                    first,
                    second
                ),
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
        val first = FakeSpanExporter(exportReturnValue = { throw IllegalStateException() })
        val second = FakeSpanExporter()
        val exporter =
            CompositeSpanExporter(
                listOf(
                    first,
                    second
                ),
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
        val first = FakeSpanExporter(forceFlushReturnValue = { throw IllegalStateException() })
        val second = FakeSpanExporter()
        val exporter =
            CompositeSpanExporter(
                listOf(
                    first,
                    second
                ),
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
        val first = FakeSpanExporter(shutdownReturnValue = { throw IllegalStateException() })
        val second = FakeSpanExporter()
        val exporter =
            CompositeSpanExporter(
                listOf(
                    first,
                    second
                ),
                errorHandler
            )
        exporter.assertReturnValuesMatch(
            Success,
            Success,
            Failure
        )
        assertTelemetryCapturedFailure(first, second)
    }

    private fun CompositeSpanExporter.assertReturnValuesMatch(
        export: OperationResultCode,
        flush: OperationResultCode,
        shutdown: OperationResultCode
    ) {
        assertEquals(shutdown, shutdown())
        assertEquals(flush, forceFlush())
        assertEquals(export, export(fakeTelemetry))
    }

    private fun assertTelemetryCapturedSuccess(
        first: FakeSpanExporter,
        second: FakeSpanExporter
    ) {
        assertFalse(errorHandler.hasErrors())
        assertSame(fakeTelemetry.single(), first.exports.single())
        assertSame(fakeTelemetry.single(), second.exports.single())
    }

    private fun assertTelemetryCapturedFailure(
        first: FakeSpanExporter,
        second: FakeSpanExporter
    ) {
        assertTrue(errorHandler.hasErrors())
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertSame(fakeTelemetry.single(), first.exports.single())
        assertSame(fakeTelemetry.single(), second.exports.single())
    }
}
