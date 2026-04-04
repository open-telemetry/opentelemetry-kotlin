package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.SamplerConfigDsl
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import io.opentelemetry.kotlin.tracing.TracerImpl
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.fakeSpanLimitsConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BuiltInSamplersTest {

    private val clock = FakeClock()
    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val contextFactory = ContextFactoryImpl(spanContextFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory, contextFactory.spanKey)
    private val scope = InstrumentationScopeInfoImpl("test", null, null, emptyMap())

    private val samplerDsl = object : SamplerConfigDsl {
        override val spanFactory = this@BuiltInSamplersTest.spanFactory
    }

    private fun buildTracer(sampler: Sampler) = TracerImpl(
        clock = clock,
        processor = FakeSpanProcessor(),
        contextFactory = contextFactory,
        spanContextFactory = spanContextFactory,
        traceFlagsFactory = traceFlagsFactory,
        spanFactory = spanFactory,
        scope = scope,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = idGenerator,
        shutdownState = MutableShutdownState(),
        sampler = sampler,
    )

    private fun contextWithParent(sampled: Boolean, isRemote: Boolean): Context {
        val traceFlags = when {
            sampled -> traceFlagsFactory.default
            else -> TraceFlagsImpl(isSampled = false, isRandom = false)
        }
        val parentSpanContext = spanContextFactory.create(
            traceId = "12345678901234567890123456789012",
            spanId = "1234567890123456",
            traceFlags = traceFlags,
            traceState = traceStateFactory.default,
            isRemote = isRemote,
        )
        val parentSpan = NonRecordingSpan(spanContextFactory.invalid, parentSpanContext)
        return contextFactory.storeSpan(contextFactory.root(), parentSpan)
    }

    @Test
    fun testAlwaysOnRecordsAndSamplesSpan() {
        val span = buildTracer(AlwaysOnSampler(spanFactory)).startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testAlwaysOffDropsSpan() {
        val span = buildTracer(AlwaysOffSampler(spanFactory)).startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testParentBasedDescription() {
        val sampler = samplerDsl.parentBased(root = samplerDsl.alwaysOn())
        assertEquals(
            "ParentBased{root:AlwaysOnSampler,remoteParentSampled:AlwaysOnSampler," +
                "remoteParentNotSampled:AlwaysOffSampler,localParentSampled:AlwaysOnSampler," +
                "localParentNotSampled:AlwaysOffSampler}",
            sampler.description
        )
    }

    @Test
    fun testParentBasedRootSampler() {
        val tracer = buildTracer(samplerDsl.parentBased(root = samplerDsl.alwaysOff()))
        val span = tracer.startSpan("root")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testParentBasedRemoteParentSampled() {
        val tracer = buildTracer(samplerDsl.parentBased(root = samplerDsl.alwaysOff()))
        val span = tracer.startSpan("child", parentContext = contextWithParent(sampled = true, isRemote = true))
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testParentBasedRemoteParentNotSampled() {
        val tracer = buildTracer(samplerDsl.parentBased(root = samplerDsl.alwaysOn()))
        val span = tracer.startSpan("child", parentContext = contextWithParent(sampled = false, isRemote = true))
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testParentBasedLocalParentSampled() {
        val tracer = buildTracer(samplerDsl.parentBased(root = samplerDsl.alwaysOff()))
        val span = tracer.startSpan("child", parentContext = contextWithParent(sampled = true, isRemote = false))
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun testParentBasedLocalParentNotSampled() {
        val tracer = buildTracer(samplerDsl.parentBased(root = samplerDsl.alwaysOn()))
        val span = tracer.startSpan("child", parentContext = contextWithParent(sampled = false, isRemote = false))
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }
}
