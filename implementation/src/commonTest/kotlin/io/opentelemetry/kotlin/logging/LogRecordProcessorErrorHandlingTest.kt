package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class LogRecordProcessorErrorHandlingTest {

    private val key = InstrumentationScopeInfoImpl("test-logger", null, null, emptyMap())
    private lateinit var processor: FakeLogRecordProcessor
    private lateinit var errorHandler: FakeSdkErrorHandler
    private lateinit var logger: LoggerImpl

    @BeforeTest
    fun setUp() {
        processor = FakeLogRecordProcessor()
        errorHandler = FakeSdkErrorHandler()
        logger = LoggerImpl(
            clock = FakeClock(),
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            key = key,
            resource = FakeResource(),
            logLimits = fakeLogLimitsConfig,
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = errorHandler,
        )
    }

    @Test
    fun testOnEmitThrowsIsContained() {
        processor.action = { _, _ -> throw IllegalStateException("boom") }

        logger.emit("message")

        assertSingleError()
        assertEquals(1, processor.logs.size)
    }

    @Test
    fun testOnEmitThrowsDoesNotPreventSubsequentEmits() {
        processor.action = { _, _ -> throw IllegalStateException("boom") }

        logger.emit("first")
        logger.emit("second")

        assertEquals(2, errorHandler.userCodeErrors.size)
        assertEquals(2, processor.logs.size)
    }

    @Test
    fun testEnabledThrowsFailsOpen() {
        processor.enabledResult = { throw IllegalStateException("boom") }

        assertTrue(logger.enabled())

        assertSingleError()
    }

    private fun assertSingleError() {
        val error = errorHandler.userCodeErrors.single()
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals("boom", error.cause.message)
    }
}
