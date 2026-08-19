package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggerEnabledTest {

    private val key = InstrumentationScopeInfoImpl("test-logger", null, null, emptyMap())
    private lateinit var clock: FakeClock

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
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

    @Test
    fun testSeverityBelowMinimumSeverity() {
        val logger = createLogger(config = LoggerConfigImpl(minimumSeverity = SeverityNumber.WARN))
        assertFalse(logger.enabled(severityNumber = SeverityNumber.INFO))
    }

    @Test
    fun testSeverityAtOrAboveMinimumSeverity() {
        val logger = createLogger(config = LoggerConfigImpl(minimumSeverity = SeverityNumber.WARN))
        assertTrue(logger.enabled(severityNumber = SeverityNumber.WARN))
        assertTrue(logger.enabled(severityNumber = SeverityNumber.ERROR))
    }

    @Test
    fun testUnspecifiedSeverityIgnoresMinimumSeverity() {
        val logger = createLogger(config = LoggerConfigImpl(minimumSeverity = SeverityNumber.WARN))
        assertTrue(logger.enabled())
        assertTrue(logger.enabled(severityNumber = SeverityNumber.UNKNOWN))
    }

    @Test
    fun testTraceBasedWithUnsampledTrace() {
        val logger = createLogger(config = LoggerConfigImpl(traceBased = true))
        assertFalse(logger.enabled(context = contextWithSpan(sampled = false)))
    }

    @Test
    fun testTraceBasedWithSampledTrace() {
        val logger = createLogger(config = LoggerConfigImpl(traceBased = true))
        assertTrue(logger.enabled(context = contextWithSpan(sampled = true)))
    }

    @Test
    fun testTraceBasedWithoutTrace() {
        val logger = createLogger(config = LoggerConfigImpl(traceBased = true))
        assertTrue(logger.enabled(context = FakeContext()))
    }

    @Test
    fun testUnsampledTraceWithoutTraceBased() {
        val logger = createLogger(config = LoggerConfigImpl(traceBased = false))
        assertTrue(logger.enabled(context = contextWithSpan(sampled = false)))
    }

    private fun contextWithSpan(sampled: Boolean): FakeContext {
        val spanContext = FakeSpanContext(
            traceIdBytes = FakeSpanContext.VALID.traceIdBytes,
            spanIdBytes = FakeSpanContext.VALID.spanIdBytes,
            traceFlags = FakeTraceFlags(isSampled = sampled),
        )
        return FakeContext(span = FakeSpan(spanContext = spanContext))
    }

    private fun createLogger(
        processor: FakeLogRecordProcessor? = FakeLogRecordProcessor(enabledResult = { true }),
        config: LoggerConfig = LoggerConfigImpl(),
    ): LoggerImpl {
        val logger = LoggerImpl(
            clock = clock,
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            key = key,
            resource = FakeResource(),
            logLimitConfig = fakeLogLimitsConfig,
            shutdownState = MutableShutdownState(),
            loggerConfig = config,
            sdkErrorHandler = NoopSdkErrorHandler,
        )
        return logger
    }
}
