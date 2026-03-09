package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.factory.FakeTraceStateFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SpanEndTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        tracer = TracerImpl(
            clock = clock,
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            traceStateFactory = FakeTraceStateFactory(),
            spanFactory = FakeSpanFactory(),
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = MutableShutdownState(),
        )
    }

    @Test
    fun testSpanEndWithExplicitTimestamp() {
        val timestamp = 100L
        val span = tracer.startSpan("test")
        span.end(timestamp)
        assertSpanTimestamp(timestamp)
    }

    @Test
    fun testSpanEndWithImplicitTimestamp() {
        val timestamp = 50L
        clock.time = timestamp
        val span = tracer.startSpan("test")
        span.end()
        assertSpanTimestamp(timestamp)
    }

    @Test
    fun testSpanIsRecording() {
        val span = tracer.startSpan("test")
        assertTrue(span.isRecording())
        span.end()
        assertFalse(span.isRecording())
    }

    @Test
    fun testMultipleEndCalls() {
        val span = tracer.startSpan("test")
        assertTrue(span.isRecording())

        val timestamp = 100L
        span.end(timestamp)
        assertFalse(span.isRecording())

        span.end(80)
        assertFalse(span.isRecording())

        span.end()
        assertFalse(span.isRecording())

        assertSpanTimestamp(timestamp)
    }

    @Test
    fun testSpanProcessorContainingEndCall() {
        var startCallCount = 0
        var endCallCount = 0

        processor.startAction = { rwSpan, _ ->
            startCallCount++
            assertTrue(rwSpan.isRecording())
            rwSpan.end()
        }
        processor.endAction = { rSpan ->
            endCallCount++
            assertTrue(rSpan.hasEnded)
        }

        val span = tracer.startSpan("test")
        assertFalse(span.isRecording())
        span.end()
        assertFalse(span.isRecording())

        assertEquals(1, startCallCount)
        assertEquals(1, endCallCount)
        assertSpanTimestamp(clock.now())
    }

    private fun assertSpanTimestamp(timestamp: Long) {
        val readableSpan = processor.startCalls.single()
        assertEquals(timestamp, readableSpan.endTimestamp)

        val endedSpan = processor.endCalls.single()
        assertEquals(timestamp, endedSpan.endTimestamp)
    }
}
