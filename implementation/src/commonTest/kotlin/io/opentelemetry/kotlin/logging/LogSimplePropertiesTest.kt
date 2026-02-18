package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class LogSimplePropertiesTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var logger: LoggerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeLogRecordProcessor
    private lateinit var sdkFactory: SdkFactory

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeLogRecordProcessor()
        sdkFactory = FakeSdkFactory()
        logger = LoggerImpl(
            clock,
            processor,
            sdkFactory,
            key,
            FakeResource(),
            fakeLogLimitsConfig
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
