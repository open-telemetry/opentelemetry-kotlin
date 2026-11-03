package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.tracing.createSpanContext
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatLoggerErrorHandlingTest {

    private val sdks = SdkTracker()

    private val errorHandler = FakeSdkErrorHandler()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

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
        }.also(sdks::track).loggerProvider
        loggerProvider.getLogger("test") { boom() }.emit(body = "log")

        assertTrue(processor.logs.isEmpty())
        assertSingleError("LoggerProvider.getLogger failed")
    }

    @Test
    fun `unsupported log record writes are ignored and reported once per property`() {
        val processor = FakeLogRecordProcessor(action = { log, _ -> log.writeAllProperties() })
        val logger = createLogger(processor)
        logger.emit(body = "first", severityText = "INFO")
        logger.emit(body = "second", severityText = "INFO")

        val expected = listOf(
            "timestamp",
            "observedTimestamp",
            "severityNumber",
            "severityText",
            "body",
            "spanContext",
            "eventName",
        ).map { "ReadWriteLogRecord.$it" }
        assertEquals(expected.size, errorHandler.errors.size)
        assertEquals(expected, errorHandler.apiMisuses.map { it.api })
        errorHandler.errors.forEach { assertEquals(SdkErrorSeverity.WARNING, it.severity) }
        assertEquals(listOf("first", "second"), processor.logs.map { it.body })
        assertEquals("INFO", processor.logs.first().severityText)
    }

    @Test
    fun `log record attribute writes are applied and not reported`() {
        val processor = FakeLogRecordProcessor(action = { log, _ -> log.setStringAttribute("key", "value") })
        createLogger(processor).emit(body = "log")

        assertEquals("value", processor.logs.single().attributes["key"])
        assertTrue(errorHandler.errors.isEmpty())
    }

    private fun createLogger(vararg processors: FakeLogRecordProcessor): Logger =
        createCompatOpenTelemetry {
            errorHandler(errorHandler)
            loggerProvider {
                processors.forEach { processor -> export { processor } }
            }
        }.also(sdks::track).loggerProvider.getLogger("test")

    private fun assertSingleError(message: String) {
        assertEquals(1, errorHandler.errors.size)
        val error = errorHandler.userCodeErrors.single()
        assertEquals(message, error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals("boom", error.cause.message)
    }

    private fun ReadWriteLogRecord.writeAllProperties() {
        timestamp = 1
        observedTimestamp = 2
        severityNumber = SeverityNumber.ERROR
        severityText = "ERROR"
        body = "changed"
        spanContext = createSpanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331")
        eventName = "event"
    }

    private fun boom(): Nothing = error("boom")
}
