package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactory
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TracerSamplerTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor
    private lateinit var contextFactory: ContextFactory
    private lateinit var spanContextFactory: SpanContextFactory
    private lateinit var spanFactory: SpanFactory
    private lateinit var idGenerator: IdGenerator

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        idGenerator = IdGeneratorImpl()
        val traceFlags = TraceFlagsFactoryImpl()
        val traceState = TraceStateFactoryImpl()
        spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlags, traceState)
        contextFactory = ContextFactoryImpl()
        spanFactory = SpanFactoryImpl(spanContextFactory, (contextFactory as ContextFactoryImpl).spanKey)
    }

    private fun buildTracer(sampler: Sampler = AlwaysOnSampler(spanFactory)) = TracerImpl(
        clock = clock,
        processor = processor,
        contextFactory = contextFactory,
        spanContextFactory = spanContextFactory,
        traceFlagsFactory = TraceFlagsFactoryImpl(),
        traceStateFactory = TraceStateFactoryImpl(),
        spanFactory = spanFactory,
        scope = key,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = idGenerator,
        shutdownState = MutableShutdownState(),
        sampler = sampler,
    )

    @Test
    fun testDefaultSamplerSpanIsSampled() {
        val tracer = buildTracer()
        val span = tracer.startSpan("test")
        assertTrue(span.spanContext.traceFlags.isSampled)
        assertTrue(span.isRecording())
    }

    @Test
    fun testDropDecisionReturnsNoopSpan() {
        val sampler = FakeSampler(SamplingResult.Decision.DROP)
        val tracer = buildTracer(sampler)
        val span = tracer.startSpan("test")
        assertFalse(span.isRecording())
    }

    @Test
    fun testRecordOnlyDecisionSpanIsNotSampled() {
        val sampler = FakeSampler(SamplingResult.Decision.RECORD_ONLY)
        val tracer = buildTracer(sampler)
        val span = tracer.startSpan("test")
        assertTrue(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testRecordAndSampleDecisionSpanIsSampled() {
        val sampler = FakeSampler(SamplingResult.Decision.RECORD_AND_SAMPLE)
        val tracer = buildTracer(sampler)
        val span = tracer.startSpan("test")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }
}
