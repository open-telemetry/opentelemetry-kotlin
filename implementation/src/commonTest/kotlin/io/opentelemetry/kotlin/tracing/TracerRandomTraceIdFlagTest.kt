package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.propagation.FakeTextMapSetter
import io.opentelemetry.kotlin.propagation.W3CTraceContextPropagator
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.implementation.createSpanContext
import io.opentelemetry.kotlin.tracing.model.hex
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The SDK sets the W3C Trace Context Level 2 random flag when the [IdGenerator] declares that its
 * trace IDs meet the randomness requirements.
 */
internal class TracerRandomTraceIdFlagTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())

    @Test
    fun testInterfaceDefaultIsNotRandom() {
        assertFalse(NonRandomIdGenerator().generatesRandomTraceIds)
    }

    @Test
    fun testRootSpanOfRandomIdGeneratorHasRandomFlag() {
        val span = buildTracer(IdGeneratorImpl()).startSpan("test")
        assertTrue(span.spanContext.traceFlags.isRandom)
        assertEquals("03", span.spanContext.traceFlags.hex)
    }

    @Test
    fun testRootSpanOfNonRandomIdGeneratorHasNoRandomFlag() {
        val span = buildTracer(NonRandomIdGenerator()).startSpan("test")
        assertFalse(span.spanContext.traceFlags.isRandom)
        assertEquals("01", span.spanContext.traceFlags.hex)
    }

    @Test
    fun testUnsampledRootSpanRetainsRandomFlag() {
        val sampler = FakeSampler(SamplingResult.Decision.DROP)
        val span = buildTracer(IdGeneratorImpl(), sampler).startSpan("test")
        assertTrue(span.spanContext.traceFlags.isRandom)
        assertEquals("02", span.spanContext.traceFlags.hex)
    }

    @Test
    fun testRandomFlagIsPropagatedInTraceParentHeader() {
        val idGenerator = IdGeneratorImpl()
        val spanFactory = SpanFactoryImpl()
        val propagator = W3CTraceContextPropagator(spanFactory = spanFactory)
        val span = buildTracer(idGenerator).startSpan("test")
        val carrier = mutableMapOf<String, String>()
        propagator.inject(ContextFactoryImpl(spanFactory).root().storeSpan(span), carrier, FakeTextMapSetter)

        val spanContext = span.spanContext
        assertEquals("00-${spanContext.traceId}-${spanContext.spanId}-03", carrier["traceparent"])
    }

    @Test
    fun testChildSpanInheritsRandomFlagFromParent() {
        val span = startChildOfRemoteParent(NonRandomIdGenerator(), parentIsRandom = true)
        assertTrue(span.spanContext.traceFlags.isRandom)
        assertEquals("03", span.spanContext.traceFlags.hex)
    }

    @Test
    fun testChildSpanInheritsAbsentRandomFlagFromParent() {
        val span = startChildOfRemoteParent(IdGeneratorImpl(), parentIsRandom = false)
        assertFalse(span.spanContext.traceFlags.isRandom)
        assertEquals("01", span.spanContext.traceFlags.hex)
    }

    private fun startChildOfRemoteParent(idGenerator: IdGenerator, parentIsRandom: Boolean): Span {
        val spanFactory = SpanFactoryImpl()
        val parent = createSpanContext("12345678901234567890123456789012", "1234567890123456") {
            isSampled = true
            isRandom = parentIsRandom
            isRemote = true
        }
        val parentContext = ContextFactoryImpl(spanFactory).root().storeSpan(spanFactory.fromSpanContext(parent))
        return buildTracer(idGenerator).startSpan("test", parentContext = parentContext)
    }

    private fun buildTracer(
        idGenerator: IdGenerator,
        sampler: Sampler = AlwaysOnSampler,
    ): TracerImpl {
        return TracerImpl(
            clock = FakeClock(),
            processor = FakeSpanProcessor(),
            contextFactory = ContextFactoryImpl(SpanFactoryImpl()),
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = idGenerator,
            shutdownState = MutableShutdownState(),
            sampler = sampler,
            sdkErrorHandler = NoopSdkErrorHandler,
        )
    }

    /**
     * Deliberately does not override [IdGenerator.generatesRandomTraceIds], pinning the default.
     */
    private class NonRandomIdGenerator(private val impl: IdGenerator = IdGeneratorImpl()) : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = impl.generateSpanIdBytes()
        override fun generateTraceIdBytes(): ByteArray = impl.generateTraceIdBytes()
        override val invalidTraceId: ByteArray = impl.invalidTraceId
        override val invalidSpanId: ByteArray = impl.invalidSpanId
    }
}
