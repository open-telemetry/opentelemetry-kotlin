package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaClock
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.logging.LoggerProviderAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ReadWriteLogRecordAdapterTest {

    private val sdkClock = FakeOtelJavaClock(start = 1_000_000)
    private val processor = FakeOtelJavaLogRecordProcessor()
    private val errorHandler = FakeSdkErrorHandler()

    private val logger = LoggerProviderAdapter(
        OtelJavaSdkLoggerProvider.builder()
            .setClock(sdkClock)
            .addLogRecordProcessor(processor)
            .build(),
        errorHandler,
    ).getLogger("test")

    private val adapter: ReadWriteLogRecordAdapter
        get() = ReadWriteLogRecordAdapter(
            processor.exports.single(),
            UnsupportedLogRecordWriteReporter(errorHandler),
        )

    @Test
    fun `attribute removal is reported and ignored`() {
        logger.emit(body = "log")
        val record = adapter
        record.setStringAttribute("key", "value")

        record.removeAttribute("key")

        assertEquals("value", record.attributes["key"])
        val error = errorHandler.apiMisuses.single()
        assertEquals("ReadWriteLogRecord.attributes", error.api)
        assertEquals(
            "opentelemetry-java does not support removing attributes, so the removal was ignored.",
            error.message,
        )
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun `attribute removal reports once per processor`() {
        logger.emit(body = "log")
        val record = adapter

        record.removeAttribute("first")
        record.removeAttribute("second")

        assertEquals(1, errorHandler.apiMisuses.size)
    }
}
