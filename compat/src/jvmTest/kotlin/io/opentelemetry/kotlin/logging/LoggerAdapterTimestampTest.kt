package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaClock
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.logging.export.ReadWriteLogRecordAdapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class LoggerAdapterTimestampTest {

    private val sdkClock = FakeOtelJavaClock(start = 1_000_000)
    private val processor = FakeOtelJavaLogRecordProcessor()

    private val logger = LoggerProviderAdapter(
        OtelJavaSdkLoggerProvider.builder()
            .setClock(sdkClock)
            .addLogRecordProcessor(processor)
            .build(),
        NoopSdkErrorHandler,
    ).getLogger("test")

    private val emitted: OtelJavaLogRecordData
        get() = processor.exports.single().toLogRecordData()

    @Test
    fun `log without timestamps is observed by the sdk clock`() {
        logger.emit(body = "log")
        assertEquals(0, emitted.timestampEpochNanos)
        assertEquals(1_000_000, emitted.observedTimestampEpochNanos)
    }

    @Test
    fun `explicit timestamps are kept`() {
        logger.emit(body = "log", timestamp = 42, observedTimestamp = 50)
        assertEquals(42, emitted.timestampEpochNanos)
        assertEquals(50, emitted.observedTimestampEpochNanos)
    }

    @Test
    fun `zero observed timestamp is observed by the sdk clock`() {
        logger.emit(body = "log", timestamp = 0, observedTimestamp = 0)
        assertEquals(0, emitted.timestampEpochNanos)
        assertEquals(1_000_000, emitted.observedTimestampEpochNanos)
    }

    @Test
    fun `unset timestamp is null when read through the kotlin api`() {
        logger.emit(body = "log")
        val record = ReadWriteLogRecordAdapter(processor.exports.single())
        assertNull(record.timestamp)
        assertEquals(1_000_000, record.observedTimestamp)
    }

    @Test
    fun `negative timestamps are treated as unset`() {
        logger.emit(body = "log", timestamp = -1, observedTimestamp = -1)
        assertEquals(0, emitted.timestampEpochNanos)
        assertEquals(1_000_000, emitted.observedTimestampEpochNanos)
    }
}
