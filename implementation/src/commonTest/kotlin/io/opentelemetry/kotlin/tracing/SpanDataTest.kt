package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class SpanDataTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor
    private lateinit var fakeResource: FakeResource
    private lateinit var fakeSpanContext: FakeSpanContext

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        fakeResource = FakeResource()
        fakeSpanContext = FakeSpanContext.INVALID
        tracer = TracerImpl(
            clock,
            processor,
            FakeSdkFactory(),
            key,
            fakeResource,
            fakeSpanLimitsConfig
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

    private fun simulateSpan(): Span {
        return tracer.startSpan(
            name = "test",
            spanKind = SpanKind.CLIENT,
            startTimestamp = 5,
        ).apply {
            status = StatusData.Error("Whoops")
            setStringAttribute("string", "value")
            addEvent("event", 10) {
                setStringAttribute("string", "value")
            }
            addLink(fakeSpanContext) {
                setStringAttribute("string", "value")
            }
            end()
        }
    }

    private fun assertSpanData(
        span: Span,
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
