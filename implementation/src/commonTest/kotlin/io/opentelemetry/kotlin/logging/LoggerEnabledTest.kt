package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.factory.SdkFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class LoggerEnabledTest {

    private val key = InstrumentationScopeInfoImpl("test-logger", null, null, emptyMap())
    private lateinit var clock: FakeClock
    private lateinit var sdkFactory: SdkFactory

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        sdkFactory = FakeSdkFactory()
    }

    @Test
    fun testNoProcessor() {
        val logger = createLogger(null)
        assertFalse(logger.enabled())
    }

    @Test
    fun testProcessorEnabled() {
        val processor = FakeLogRecordProcessor(enabledResult = { true })
        val logger = createLogger(processor)
        assertTrue(logger.enabled())
    }

    @Test
    fun testProcessorDisabled() {
        val processor = FakeLogRecordProcessor(enabledResult = { false })
        val logger = createLogger(processor)
        assertFalse(logger.enabled())
    }

    @Test
    fun testProcessorEnabledWithExplicitContext() {
        val processor = FakeLogRecordProcessor(enabledResult = { true })
        val logger = createLogger(processor)
        assertTrue(logger.enabled(context = FakeContext()))
    }

    private fun createLogger(processor: FakeLogRecordProcessor?): LoggerImpl {
        val logger = LoggerImpl(
            clock,
            processor,
            sdkFactory,
            key,
            FakeResource(),
            fakeLogLimitsConfig
        )
        return logger
    }
}
