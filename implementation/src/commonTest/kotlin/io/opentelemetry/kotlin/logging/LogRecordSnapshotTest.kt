package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame

internal class LogRecordSnapshotTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private val processor = FakeLogRecordProcessor()
    private lateinit var logger: LoggerImpl

    @BeforeTest
    fun setUp() {
        logger = LoggerImpl(
            clock = FakeClock(),
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            key = key,
            resource = FakeResource(),
            logLimitConfig = LogLimitConfig(
                attributeCountLimit = 2,
                attributeValueLengthLimit = fakeLogLimitsConfig.attributeValueLengthLimit
            ),
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = NoopSdkErrorHandler,
        )
    }

    @Test
    fun testSnapshotHoldsPlainData() {
        logger.emit(
            body = "my_log",
            eventName = "my_event",
            severityNumber = SeverityNumber.WARN,
            severityText = "warning",
        ) {
            setStringAttribute("key", "value")
        }

        val log = processor.logs.single()
        val data = log.toLogRecordData()
        assertIs<LogRecordDataImpl>(data)

        val emitted: Any = log
        assertNotSame(data, emitted)

        assertEquals("my_log", data.body)
        assertEquals("my_event", data.eventName)
        assertEquals(SeverityNumber.WARN, data.severityNumber)
        assertEquals("warning", data.severityText)
        assertEquals(mapOf("key" to "value"), data.attributes)
        assertEquals(log.timestamp, data.timestamp)
        assertEquals(log.observedTimestamp, data.observedTimestamp)
        assertEquals(log.spanContext, data.spanContext)
        assertEquals(log.resource, data.resource)
        assertEquals(key, data.instrumentationScopeInfo)
    }

    @Test
    fun testSnapshotUnaffectedByLaterMutation() {
        logger.emit("my_log") { setStringAttribute("key", "value") }

        val log = processor.logs.single()
        val data = log.toLogRecordData()

        log.body = "changed"
        log.severityText = "changed"
        log.setStringAttribute("other", "value")

        assertEquals("my_log", data.body)
        assertEquals(mapOf("key" to "value"), data.attributes)
    }

    @Test
    fun testSnapshotCapturesDroppedAttributesCount() {
        logger.emit("my_log") {
            setStringAttribute("a", "value")
            setStringAttribute("b", "value")
            setStringAttribute("c", "value") // exceeds the limit of 2
        }

        val data = processor.logs.single().toLogRecordData()
        assertEquals(2, data.attributes.size)
        assertEquals(1, data.droppedAttributesCount)
    }
}
