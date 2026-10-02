package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaClock
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanProcessor
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.toOtelKotlinApi
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TracerAdapterTimestampTest {

    private val sdkClock = SteppingClock(start = 1_000_000)
    private val ended = mutableListOf<OtelJavaSpanData>()

    private val tracer = TracerProviderAdapter(
        OtelJavaSdkTracerProvider.builder()
            .setClock(sdkClock)
            .addSpanProcessor(CapturingProcessor(ended))
            .build(),
        CompatSpanLimitsConfig(),
        CompatContextFactory(),
        NoopSdkErrorHandler,
    ).getTracer("test")

    @Test
    fun `span without explicit timestamps is timed by the sdk clock at both ends`() {
        val span = tracer.startSpan("span")
        sdkClock.advance(500)
        span.end()

        val data = ended.single()
        assertEquals(1_000_000, data.startEpochNanos)
        assertEquals(1_000_500, data.endEpochNanos)
    }

    @Test
    fun `explicit start timestamp is kept`() {
        tracer.startSpan("span", startTimestamp = 42).end(100)

        val data = ended.single()
        assertEquals(42, data.startEpochNanos)
        assertEquals(100, data.endEpochNanos)
    }

    @Test
    fun `zero start timestamp is timed by the sdk clock`() {
        tracer.startSpan("span", startTimestamp = 0).end()
        assertEquals(1_000_000, ended.single().startEpochNanos)
    }

    @Test
    fun `negative start timestamp is timed by the sdk clock`() {
        tracer.startSpan("span", startTimestamp = -1).end()
        assertEquals(1_000_000, ended.single().startEpochNanos)
    }

    @Test
    fun `invalid end timestamp is timed by the sdk clock`() {
        val zero = tracer.startSpan("zero")
        val negative = tracer.startSpan("negative")
        sdkClock.advance(500)
        zero.end(0)
        negative.end(-1)

        assertEquals(listOf(1_000_500L, 1_000_500L), ended.map { it.endEpochNanos })
    }

    @Test
    fun `invalid event timestamp is timed by the sdk clock`() {
        val span = tracer.startSpan("span")
        sdkClock.advance(200)
        span.addEvent("zero", 0)
        span.addEvent("negative", -1)
        span.end()

        assertEquals(listOf(1_000_200L, 1_000_200L), ended.single().events.map { it.epochNanos })
    }

    @Test
    fun `event without explicit timestamp is timed by the sdk clock`() {
        val span = tracer.startSpan("span")
        sdkClock.advance(200)
        span.addEvent("event")
        span.end()

        assertEquals(1_000_200, ended.single().events.single().epochNanos)
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun `child span is timed by one clock even when given the sdk clock`() {
        val clock = SteppingClock(start = 1_000_000)
        val spans = mutableListOf<OtelJavaSpanData>()
        val provider = OtelJavaSdkTracerProvider.builder()
            .setClock(clock)
            .addSpanProcessor(CapturingProcessor(spans))
            .build()
        val kotlinTracer = OtelJavaOpenTelemetrySdk.builder().setTracerProvider(provider).build()
            .toOtelKotlinApi(clock = { clock.now() })
            .tracerProvider.getTracer("test")

        val parent = provider.get("java").spanBuilder("parent").startSpan()
        val scope = parent.makeCurrent()
        try {
            // The wall clock steps ahead of the monotonic one, as it does under NTP adjustment.
            clock.advance(wall = 1_000, mono = 10)
            val child = kotlinTracer.startSpan("child")
            clock.advance(5)
            child.end()
        } finally {
            scope.close()
            parent.end()
        }

        val child = spans.first { it.name == "child" }
        assertEquals(1_000_010, child.startEpochNanos)
        assertEquals(1_000_015, child.endEpochNanos)
    }

    @OptIn(ExperimentalApi::class)
    @Test
    fun `event on child span is timed by the parent's anchored clock`() {
        val clock = SteppingClock(start = 1_000_000)
        val spans = mutableListOf<OtelJavaSpanData>()
        val provider = OtelJavaSdkTracerProvider.builder()
            .setClock(clock)
            .addSpanProcessor(CapturingProcessor(spans))
            .build()
        val kotlinTracer = OtelJavaOpenTelemetrySdk.builder().setTracerProvider(provider).build()
            .toOtelKotlinApi(clock = { clock.now() })
            .tracerProvider.getTracer("test")

        val parent = kotlinTracer.startSpan("parent")
        val child = kotlinTracer.startSpan("child", parentContext = CompatContextFactory().root().storeSpan(parent))
        clock.advance(wall = 1_000, mono = 200)
        child.addEvent("event")
        child.end()
        parent.end()

        val event = spans.first { it.name == "child" }.events.single()
        assertEquals(1_000_200, event.epochNanos)
    }

    private class SteppingClock(start: Long) : OtelJavaClock {
        private var wall = start
        private var mono = start

        fun advance(wall: Long, mono: Long = wall) {
            this.wall += wall
            this.mono += mono
        }

        override fun now(): Long = wall
        override fun nanoTime(): Long = mono
    }

    private class CapturingProcessor(private val sink: MutableList<OtelJavaSpanData>) : OtelJavaSpanProcessor {
        override fun onStart(parentContext: OtelJavaContext, span: OtelJavaReadWriteSpan) = Unit
        override fun isStartRequired(): Boolean = false
        override fun onEnd(span: OtelJavaReadableSpan) {
            sink += span.toSpanData()
        }
        override fun isEndRequired(): Boolean = true
    }
}
