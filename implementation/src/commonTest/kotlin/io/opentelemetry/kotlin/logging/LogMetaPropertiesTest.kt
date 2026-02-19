package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
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
            clock,
            processor,
            FakeSdkFactory(),
            key,
            fakeResource,
            fakeLogLimitsConfig
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
