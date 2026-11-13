package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanData
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OtelJavaSpanExporterAdapterTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `export converts spans and delegates`() {
        val impl = FakeSpanExporter()
        val adapter = OtelJavaSpanExporterAdapter(impl, errorHandler)

        val spans = mutableListOf<OtelJavaSpanData>(FakeOtelJavaSpanData(implName = "converted"))
        assertTrue(adapter.export(spans).await().isSuccess)
        assertEquals("converted", impl.exports.single().name)
    }

    @Test
    fun `operation failures are propagated`() {
        val impl = FakeSpanExporter(
            exportReturnValue = { OperationResultCode.Failure },
            forceFlushReturnValue = { OperationResultCode.Failure },
            shutdownReturnValue = { OperationResultCode.Failure },
        )
        val adapter = OtelJavaSpanExporterAdapter(impl, errorHandler)

        assertFalse(adapter.export(mutableListOf()).await().isSuccess)
        assertFalse(adapter.flush().await().isSuccess)
        assertFalse(adapter.shutdown().await().isSuccess)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun `exceptions are reported and fail the result`() {
        val impl = FakeSpanExporter(
            exportReturnValue = { error("export") },
            forceFlushReturnValue = { error("flush") },
            shutdownReturnValue = { error("shutdown") },
        )
        val adapter = OtelJavaSpanExporterAdapter(impl, errorHandler)

        assertFalse(adapter.export(mutableListOf()).await().isSuccess)
        assertFalse(adapter.flush().await().isSuccess)
        assertFalse(adapter.shutdown().await().isSuccess)
        assertEquals(3, errorHandler.userCodeErrors.size)
    }

    @Test
    fun `operations after shutdown fail without invoking the exporter`() {
        var exportCount = 0
        var flushCount = 0
        val impl = FakeSpanExporter(
            exportReturnValue = {
                exportCount++
                OperationResultCode.Success
            },
            forceFlushReturnValue = {
                flushCount++
                OperationResultCode.Success
            },
        )
        val adapter = OtelJavaSpanExporterAdapter(impl, errorHandler)

        assertTrue(adapter.shutdown().await().isSuccess)
        assertFalse(adapter.export(mutableListOf()).await().isSuccess)
        assertFalse(adapter.flush().await().isSuccess)
        assertEquals(0, exportCount)
        assertEquals(0, flushCount)
    }

    private fun OtelJavaCompletableResultCode.await(): OtelJavaCompletableResultCode =
        join(AWAIT_SECONDS, TimeUnit.SECONDS)

    private companion object {
        const val AWAIT_SECONDS = 5L
    }
}
