package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogSimplePropertiesTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var logger: LoggerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeLogRecordProcessor

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeLogRecordProcessor()
        logger = LoggerImpl(
            clock = clock,
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            key = key,
            resource = FakeResource(),
            logLimitConfig = fakeLogLimitsConfig,
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testMinimalLog() {
        val now = 5L
        clock.time = now
        logger.emit()

        val log = processor.logs.single()
        assertNull(log.body)
        assertEquals(now, log.timestamp)
        assertEquals(now, log.observedTimestamp)
        assertEquals(SeverityNumber.UNKNOWN, log.severityNumber)
        assertNull(log.severityText)
    }

    @Test
    fun testLogProperties() {
        val body = "Hello, World!"
        val severityText = "INFO"
        logger.emit(
            body = body,
            timestamp = 2,
            observedTimestamp = 3,
            severityNumber = SeverityNumber.INFO,
            severityText = severityText,
        )

        val log = processor.logs.single()
        assertEquals(body, log.body)
        assertEquals(2, log.timestamp)
        assertEquals(3, log.observedTimestamp)
        assertEquals(SeverityNumber.INFO, log.severityNumber)
        assertEquals(severityText, log.severityText)
    }
}
