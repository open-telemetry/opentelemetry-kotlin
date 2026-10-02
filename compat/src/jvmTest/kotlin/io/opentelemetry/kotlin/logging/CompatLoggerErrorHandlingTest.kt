package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatLoggerErrorHandlingTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `throwing attributes lambda does not escape emit`() {
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(processor)
        logger.emit(body = "log") { boom() }

        assertTrue(processor.logs.isEmpty())
        assertSingleError("Logger.emit failed")
    }

    @Test
    fun `throwing onEmit does not escape and other processors still run`() {
        val hostile = FakeLogRecordProcessor(action = { _, _ -> boom() })
        val healthy = FakeLogRecordProcessor()
        val logger = createLogger(hostile, healthy)
        logger.emit(body = "log")

        assertEquals(1, healthy.logs.size)
        assertSingleError("LogRecordProcessor.onEmit failed")
    }

    @Test
    fun `throwing scope attributes lambda does not escape getLogger`() {
        val processor = FakeLogRecordProcessor()
        val loggerProvider = createCompatOpenTelemetry {
            errorHandler(errorHandler)
            loggerProvider { export { processor } }
        }.loggerProvider
        loggerProvider.getLogger("test") { boom() }.emit(body = "log")

        assertTrue(processor.logs.isEmpty())
        assertSingleError("LoggerProvider.getLogger failed")
    }

    private fun createLogger(vararg processors: FakeLogRecordProcessor): Logger =
        createCompatOpenTelemetry {
            errorHandler(errorHandler)
            loggerProvider {
                processors.forEach { processor -> export { processor } }
            }
        }.loggerProvider.getLogger("test")

    private fun assertSingleError(message: String) {
        assertEquals(1, errorHandler.errors.size)
        val error = errorHandler.userCodeErrors.single()
        assertEquals(message, error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals("boom", error.cause.message)
    }

    private fun boom(): Nothing = error("boom")
}
