package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class LoggerProviderAdapterTest {

    private val adapter = LoggerProviderAdapter(OtelJavaSdkLoggerProvider.builder().build(), NoopSdkErrorHandler)

    @Test
    fun testDupeLoggerProviderAttributes() {
        val first = adapter.getLogger(name = "name") {
            setStringAttribute("key", "value")
        }
        val second = adapter.getLogger(name = "name") {
            setStringAttribute("key", "value")
        }
        val third = adapter.getLogger(name = "name") {
            setStringAttribute("foo", "bar")
        }
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testScopePropertyBoundaryCollision() {
        val first = adapter.getLogger(name = "ab", version = "c")
        val second = adapter.getLogger(name = "a", version = "bc")
        assertNotSame(first, second)
    }

    @Test
    fun testNullScopePropertyCollision() {
        val first = adapter.getLogger(name = "name")
        val second = adapter.getLogger(name = "name", version = "null")
        assertNotSame(first, second)
    }

    @Test
    fun testForceFlushAndShutdownDelegateToJavaSdkProvider() = runTest {
        val processor = FakeOtelJavaLogRecordProcessor()
        val provider = OtelJavaSdkLoggerProvider.builder()
            .addLogRecordProcessor(processor)
            .build()
        val adapter = LoggerProviderAdapter(provider, NoopSdkErrorHandler)

        assertEquals(OperationResultCode.Success, adapter.forceFlush())
        assertEquals(1, processor.flushCount)
        assertEquals(OperationResultCode.Success, adapter.shutdown())
        assertEquals(1, processor.shutdownCount)
    }

    @Test
    fun testForceFlushAndShutdownReportExceptions() = runTest {
        val processor = FakeOtelJavaLogRecordProcessor()
        processor.nextResult = { throw IllegalStateException("boom") }
        val errorHandler = FakeSdkErrorHandler()
        val adapter = LoggerProviderAdapter(
            OtelJavaSdkLoggerProvider.builder().addLogRecordProcessor(processor).build(),
            errorHandler,
        )

        assertEquals(OperationResultCode.Failure, adapter.forceFlush())
        assertEquals(OperationResultCode.Failure, adapter.shutdown())
        assertEquals(
            listOf("LoggerProvider.forceFlush failed", "LoggerProvider.shutdown failed"),
            errorHandler.userCodeErrors.map { it.message },
        )
    }

    @Test
    fun testForceFlushReportsExceptionalFailure() = runTest {
        val processor = FakeOtelJavaLogRecordProcessor()
        processor.nextResult = { OtelJavaCompletableResultCode.ofExceptionalFailure(IllegalStateException("boom")) }
        val errorHandler = FakeSdkErrorHandler()
        val adapter = LoggerProviderAdapter(
            OtelJavaSdkLoggerProvider.builder().addLogRecordProcessor(processor).build(),
            errorHandler,
        )

        assertEquals(OperationResultCode.Failure, adapter.forceFlush())
        assertIs<IllegalStateException>(errorHandler.userCodeErrors.single().cause)
    }
}
