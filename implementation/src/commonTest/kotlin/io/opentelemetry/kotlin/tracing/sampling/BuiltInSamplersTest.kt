package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.TracerImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.fakeSpanLimitsConfig
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BuiltInSamplersTest {

    private val clock = FakeClock()
    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val contextFactory = ContextFactoryImpl()
    private val spanFactory = SpanFactoryImpl(spanContextFactory, contextFactory.spanKey)
    private val scope = InstrumentationScopeInfoImpl("test", null, null, emptyMap())

    private fun buildTracer(builtin: BuiltInSampler) = TracerImpl(
        clock = clock,
        processor = FakeSpanProcessor(),
        contextFactory = contextFactory,
        spanContextFactory = spanContextFactory,
        traceFlagsFactory = traceFlagsFactory,
        traceStateFactory = traceStateFactory,
        spanFactory = spanFactory,
        scope = scope,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = idGenerator,
        shutdownState = MutableShutdownState(),
        sampler = builtin.toSampler(spanFactory),
    )

    @Test
    fun testAlwaysOnRecordsAndSamplesSpan() {
        val span = buildTracer(BuiltInSampler.ALWAYS_ON).startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testAlwaysOffDropsSpan() {
        val span = buildTracer(BuiltInSampler.ALWAYS_OFF).startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }
}
