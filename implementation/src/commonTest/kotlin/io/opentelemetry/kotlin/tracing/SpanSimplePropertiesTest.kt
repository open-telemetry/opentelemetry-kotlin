package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanSimplePropertiesTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        tracer = TracerImpl(
            clock,
            FakeSpanProcessor(),
            FakeSdkFactory(),
            key,
            FakeResource(),
            fakeSpanLimitsConfig
        )
    }

    @Test
    fun testSpanName() {
        val name = "test"
        val span = tracer.startSpan(name)
        assertEquals(name, span.name)
    }

    @Test
    fun testSpanNameOverride() {
        val span = tracer.startSpan("test")
        val override = "another"
        span.name = override
        assertEquals(override, span.name)
    }

    @Test
    fun testSpanNameAfterEnd() {
        val name = "test"
        val span = tracer.startSpan(name)
        span.end()
        span.name = "another"
        assertEquals(name, span.name)
    }

    @Test
    fun testSpanStatus() {
        val span = tracer.startSpan("test")
        assertEquals(StatusData.Unset, span.status)
    }

    @Test
    fun testSpanStatusOverride() {
        val span = tracer.startSpan("test")
        span.status = StatusData.Ok
        assertEquals(StatusData.Ok, span.status)
    }

    @Test
    fun testSpanStatusAfterEnd() {
        val span = tracer.startSpan("test")
        span.end()
        span.status = StatusData.Ok
        assertEquals(StatusData.Unset, span.status)
    }

    @Test
    fun testSpanKind() {
        val span = tracer.startSpan("test")
        assertEquals(SpanKind.INTERNAL, span.spanKind)
    }

    @Test
    fun testSpanKindOverride() {
        val span = tracer.startSpan(
            "test",
            spanKind = SpanKind.CLIENT,
        )
        assertEquals(SpanKind.CLIENT, span.spanKind)
    }

    @Test
    fun testSpanStartTimestamp() {
        clock.time = 5
        val span = tracer.startSpan("test")
        assertEquals(clock.time, span.startTimestamp)
    }

    @Test
    fun testSpanStartTimestampExplicit() {
        val now = 9L
        val span = tracer.startSpan("test", startTimestamp = now)
        assertEquals(now, span.startTimestamp)
    }
}
