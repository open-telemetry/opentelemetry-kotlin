package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SpanProcessorErrorHandlingTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var processor: FakeSpanProcessor
    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        processor = FakeSpanProcessor()
        errorHandler = FakeSdkErrorHandler()
        tracer = TracerImpl(
            clock = FakeClock(),
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = errorHandler,
        )
    }

    @Test
    fun testOnStartThrowsStillReturnsUsableSpan() {
        processor.startAction = { _, _ -> boom() }

        val span = tracer.startSpan("test")
        assertTrue(span.isRecording())

        assertSingleError()

        span.setStringAttribute("key", "value")
        span.end()
        assertFalse(span.isRecording())
    }

    @Test
    fun testOnEndingThrowsStillEndsSpan() {
        processor.endingAction = { boom() }

        val span = tracer.startSpan("test")
        span.end()

        assertSingleError()

        assertTrue(processor.endCalls.single().hasEnded)
        assertFalse(span.isRecording())
    }

    @Test
    fun testOnEndThrowsIsContained() {
        processor.endAction = { boom() }

        val span = tracer.startSpan("test")
        span.end()

        assertSingleError()
        assertFalse(span.isRecording())
    }

    @Test
    fun testEveryCallbackThrowsIsReportedSeparately() {
        processor.startAction = { _, _ -> boom() }
        processor.endingAction = { boom() }
        processor.endAction = { boom() }

        tracer.startSpan("test").end()

        assertEquals(3, errorHandler.userCodeErrors.size)
        assertTrue(errorHandler.userCodeErrors.all { it.cause.message == "boom" })
    }

    private fun boom(): Nothing = error("boom")

    private fun assertSingleError() {
        val error = errorHandler.userCodeErrors.single()
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals("boom", error.cause.message)
    }
}
