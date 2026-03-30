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
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SpanDataTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor
    private lateinit var fakeResource: FakeResource
    private lateinit var fakeSpanContext: FakeSpanContext
    private lateinit var shutdownState: MutableShutdownState

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        fakeResource = FakeResource()
        fakeSpanContext = FakeSpanContext.INVALID
        shutdownState = MutableShutdownState()
        tracer = TracerImpl(
            clock = clock,
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            traceStateFactory = FakeTraceStateFactory(),
            spanFactory = FakeSpanFactory(),
            scope = key,
            resource = fakeResource,
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = shutdownState,
        )
    }

    @Test
    fun testSpanDataCreationOnStart() {
        val span = simulateSpan()
        val data = processor.startCalls.single().toSpanData()
        assertSpanData(span, data)
    }

    @Test
    fun testSpanDataCreationOnEnd() {
        val span = simulateSpan()
        val data = processor.startCalls.single().toSpanData()
        assertSpanData(span, data)
    }

    @Test
    fun testRetrieveSpanData() {
        val span = tracer.startSpan("test")
        val readableSpan = span.toReadableSpan()
        val data: SpanData = readableSpan.toSpanData()
        assertEquals("test", data.name)
    }

    @Test
    fun testSpanDataAfterShutdown() {
        assertTrue(tracer.startSpan("test").isRecording())
        shutdownState.shutdownNow()
        assertFalse(tracer.startSpan("test").isRecording())
    }

    private fun simulateSpan(): ReadableSpan {
        return tracer.startSpan(
            name = "test",
            spanKind = SpanKind.CLIENT,
            startTimestamp = 5,
        ).apply {
            setStatus(StatusData.Error("Whoops"))
            setStringAttribute("string", "value")
            addEvent("event", 10) {
                setStringAttribute("string", "value")
            }
            addLink(fakeSpanContext) {
                setStringAttribute("string", "value")
            }
            end()
        }.toReadableSpan()
    }

    private fun assertSpanData(
        span: ReadableSpan,
        data: SpanData
    ) {
        assertEquals(span.name, data.name)
        assertEquals(span.spanKind, data.spanKind)
        assertEquals(span.startTimestamp, data.startTimestamp)
        assertEquals(span.status, data.status)
        assertEquals(span.attributes, data.attributes)
        assertEquals(span.events, data.events)
        assertEquals(span.links, data.links)
        assertSame(fakeResource, data.resource)
        assertSame(key, data.instrumentationScopeInfo)
    }
}
