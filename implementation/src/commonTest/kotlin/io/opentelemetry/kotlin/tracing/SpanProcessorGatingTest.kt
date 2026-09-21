package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SpanProcessorGatingTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var processor: FakeSpanProcessor
    private lateinit var errorHandler: FakeSdkErrorHandler

    @BeforeTest
    fun setUp() {
        processor = FakeSpanProcessor()
        errorHandler = FakeSdkErrorHandler()
        tracer = createTracer(processor)
    }

    private fun createTracer(processor: SpanProcessor): TracerImpl =
        TracerImpl(
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

    @Test
    fun testAllCallbacksRequiredByDefault() {
        tracer.startSpan("test").end()
        assertEquals(1, processor.startCalls.size)
        assertEquals(1, processor.endingCalls.size)
        assertEquals(1, processor.endCalls.size)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testOnStartNotRequired() {
        processor.startRequired = false
        val span = tracer.startSpan("test")
        assertTrue(span.isRecording())
        span.end()

        assertTrue(processor.startCalls.isEmpty())
        assertEquals(1, processor.endingCalls.size)
        assertEquals(1, processor.endCalls.size)
    }

    @Test
    fun testOnEndingNotRequired() {
        processor.onEndingRequired = false
        val span = tracer.startSpan("test")
        span.end()

        assertEquals(1, processor.startCalls.size)
        assertTrue(processor.endingCalls.isEmpty())
        assertEquals(1, processor.endCalls.size)
        assertFalse(span.isRecording())
    }

    @Test
    fun testOnEndNotRequired() {
        processor.endRequired = false
        val span = tracer.startSpan("test")
        span.end()

        assertEquals(1, processor.startCalls.size)
        assertEquals(1, processor.endingCalls.size)
        assertTrue(processor.endCalls.isEmpty())
        assertFalse(span.isRecording())
    }

    @Test
    fun testNoCallbacksRequired() {
        processor.startRequired = false
        processor.endRequired = false
        processor.onEndingRequired = false

        val span = tracer.startSpan("test")
        assertTrue(span.isRecording())
        span.setStringAttribute("key", "value")
        span.end()
        assertFalse(span.isRecording())

        assertTrue(processor.startCalls.isEmpty())
        assertTrue(processor.endingCalls.isEmpty())
        assertTrue(processor.endCalls.isEmpty())
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testGatingFunctionThrowsIsContained() {
        val throwing = object : SpanProcessor by processor {
            override fun isStartRequired(): Boolean = error("boom")
            override fun isEndRequired(): Boolean = error("boom")
            override fun isOnEndingRequired(): Boolean = error("boom")
        }
        val span = createTracer(throwing).startSpan("test")
        assertTrue(span.isRecording())
        span.end()
        assertFalse(span.isRecording())

        // failure is handled, not propagated
        assertEquals(3, errorHandler.userCodeErrors.size)
        assertTrue(errorHandler.userCodeErrors.all { it.cause.message == "boom" })
    }
}
