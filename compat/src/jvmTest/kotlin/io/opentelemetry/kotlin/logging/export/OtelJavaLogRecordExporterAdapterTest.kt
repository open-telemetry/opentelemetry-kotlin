package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordData
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OtelJavaLogRecordExporterAdapterTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `export converts logs and delegates`() {
        val impl = FakeLogRecordExporter()
        val adapter = OtelJavaLogRecordExporterAdapter(impl, errorHandler)

        val logs = mutableListOf<OtelJavaLogRecordData>(FakeOtelJavaLogRecordData(implTimestamp = 500L))
        assertTrue(adapter.export(logs).await().isSuccess)
        assertEquals(500L, impl.logs.single().timestamp)
    }

    @Test
    fun `operation failures are propagated`() {
        val impl = FakeLogRecordExporter(
            action = { OperationResultCode.Failure },
            flushCode = { OperationResultCode.Failure },
            shutdownCode = { OperationResultCode.Failure },
        )
        val adapter = OtelJavaLogRecordExporterAdapter(impl, errorHandler)

        assertFalse(adapter.export(mutableListOf()).await().isSuccess)
        assertFalse(adapter.flush().await().isSuccess)
        assertFalse(adapter.shutdown().await().isSuccess)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun `exceptions are reported and fail the result`() {
        val impl = FakeLogRecordExporter(
            action = { error("export") },
            flushCode = { error("flush") },
            shutdownCode = { error("shutdown") },
        )
        val adapter = OtelJavaLogRecordExporterAdapter(impl, errorHandler)

        assertFalse(adapter.export(mutableListOf()).await().isSuccess)
        assertFalse(adapter.flush().await().isSuccess)
        assertFalse(adapter.shutdown().await().isSuccess)
        assertEquals(3, errorHandler.userCodeErrors.size)
    }

    @Test
    fun `operations after shutdown fail without invoking the exporter`() {
        var exportCount = 0
        var flushCount = 0
        val impl = FakeLogRecordExporter(
            action = {
                exportCount++
                OperationResultCode.Success
            },
            flushCode = {
                flushCount++
                OperationResultCode.Success
            },
        )
        val adapter = OtelJavaLogRecordExporterAdapter(impl, errorHandler)

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
