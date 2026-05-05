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
import kotlin.test.assertSame

internal class LogMetaPropertiesTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private val fakeResource = FakeResource()
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
            resource = fakeResource,
            logLimitConfig = fakeLogLimitsConfig,
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testLogInstrumentationScope() {
        logger.emit()
        val log = processor.logs.single()
        assertSame(key, log.instrumentationScopeInfo)
    }

    @Test
    fun testLogResource() {
        logger.emit()
        val log = processor.logs.single()
        assertSame(fakeResource, log.resource)
    }
}
