package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OtelJavaLogRecordProcessorAdapterCloseableTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `forceFlush delegates to impl`() {
        var flushCount = 0
        val impl = FakeLogRecordProcessor(flushCode = {
            flushCount++
            OperationResultCode.Success
        })
        val adapter = OtelJavaLogRecordProcessorAdapter(impl, errorHandler)
        assertTrue(adapter.forceFlush().await().isSuccess)
        assertEquals(1, flushCount)
    }

    @Test
    fun `forceFlush failure is propagated`() {
        val impl = FakeLogRecordProcessor(flushCode = { OperationResultCode.Failure })
        val adapter = OtelJavaLogRecordProcessorAdapter(impl, errorHandler)
        assertFalse(adapter.forceFlush().await().isSuccess)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun `shutdown delegates to impl`() {
        var shutdownCount = 0
        val impl = FakeLogRecordProcessor(shutdownCode = {
            shutdownCount++
            OperationResultCode.Success
        })
        val adapter = OtelJavaLogRecordProcessorAdapter(impl, errorHandler)
        assertTrue(adapter.shutdown().await().isSuccess)
        assertEquals(1, shutdownCount)
    }

    @Test
    fun `exception thrown by impl is reported and fails the result`() {
        val impl = FakeLogRecordProcessor(
            flushCode = { error("flush") },
            shutdownCode = { error("shutdown") },
        )
        val adapter = OtelJavaLogRecordProcessorAdapter(impl, errorHandler)
        assertFalse(adapter.forceFlush().await().isSuccess)
        assertFalse(adapter.shutdown().await().isSuccess)
        assertEquals(2, errorHandler.userCodeErrors.size)
    }

    @Test
    fun `forceFlush after shutdown fails without calling impl`() {
        var flushCount = 0
        val impl = FakeLogRecordProcessor(flushCode = {
            flushCount++
            OperationResultCode.Success
        })
        val adapter = OtelJavaLogRecordProcessorAdapter(impl, errorHandler)
        assertTrue(adapter.shutdown().await().isSuccess)

        val result = adapter.forceFlush().await()
        assertTrue(result.isDone)
        assertFalse(result.isSuccess)
        assertEquals(0, flushCount)
    }

    private fun OtelJavaCompletableResultCode.await(): OtelJavaCompletableResultCode =
        join(AWAIT_SECONDS, TimeUnit.SECONDS)

    private companion object {
        const val AWAIT_SECONDS = 5L
    }
}
