package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class TracerSamplerTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor
    private lateinit var contextFactory: ContextFactory
    private lateinit var spanFactory: SpanFactory
    private lateinit var idGenerator: IdGenerator

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        idGenerator = IdGeneratorImpl()
        spanFactory = SpanFactoryImpl()
        contextFactory = ContextFactoryImpl(spanFactory)
    }

    private fun buildTracer(
        sampler: Sampler = AlwaysOnSampler,
        limitsCfg: SpanLimitConfig = fakeSpanLimitsConfig
    ) = TracerImpl(
        clock = clock,
        processor = processor,
        contextFactory = contextFactory,
        scope = key,
        resource = FakeResource(),
        spanLimitConfig = limitsCfg,
        idGenerator = idGenerator,
        shutdownState = MutableShutdownState(),
        sampler = sampler,
        sdkErrorHandler = NoopSdkErrorHandler,
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
        assertTrue(span.spanContext.isValid)
        assertNotEquals("0000000000000000", span.spanContext.spanId)
    }

    @Test
    fun testDropDecisionSpanIdsUnique() {
        val sampler = FakeSampler(SamplingResult.Decision.DROP)
        val tracer = buildTracer(sampler)
        val spanIds = (1..10).map { tracer.startSpan("test-$it").spanContext.spanId }.toSet()
        assertEquals(10, spanIds.size)
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

    @Test
    fun testSamplerAttrsAdded() {
        val sampler = FakeSampler(samplerAttributes = mapOf("sampler.key" to "sampler.value"))
        val tracer = buildTracer(sampler)
        val span = tracer.startSpan("test")
        assertEquals("sampler.value", span.toReadableSpan().attributes["sampler.key"])
    }

    @Test
    fun testSamplerWholeDoubleAttrsPreserved() {
        val sampler = FakeSampler(
            samplerAttributes = mapOf("double" to 2.0, "double_list" to listOf(1.0, 2.0))
        )
        val tracer = buildTracer(sampler)
        val span = tracer.startSpan("test")
        assertEquals(
            mapOf("double" to 2.0, "double_list" to listOf(1.0, 2.0)),
            span.toReadableSpan().attributes
        )
    }

    @Test
    fun testSamplerAttrsOverrideSpanAttrs() {
        val sampler = FakeSampler(samplerAttributes = mapOf("shared.key" to "sampler.value"))
        val tracer = buildTracer(sampler)
        val span = tracer.startSpan("test") {
            setStringAttribute("shared.key", "span.value")
        }
        assertEquals("sampler.value", span.toReadableSpan().attributes["shared.key"])
    }

    @Test
    fun testSamplerAttrsAtLimitOverrideButDoNotAdd() {
        val cfg = fakeSpanLimitsConfig
        val limitedConfig = SpanLimitConfig(
            attributeCountLimit = 2,
            attributeValueLengthLimit = cfg.attributeValueLengthLimit,
            linkCountLimit = cfg.linkCountLimit,
            eventCountLimit = cfg.eventCountLimit,
            attributeCountPerEventLimit = cfg.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = cfg.attributeCountPerLinkLimit,
        )
        val sampler = FakeSampler(
            samplerAttributes = mapOf(
                "b" to "sampler",
                "c" to "sampler",
            )
        )
        val tracer = buildTracer(sampler, limitedConfig)
        val span = tracer.startSpan("test") {
            setStringAttribute("a", "user")
            setStringAttribute("b", "user")
        }
        val readable = span.toReadableSpan()
        assertEquals(mapOf("a" to "user", "b" to "sampler"), readable.attributes)
        assertEquals(1, readable.droppedAttributesCount)
    }

    @Test
    fun testSamplerTraceStateApplied() {
        val traceState = FakeTraceState(mapOf("vendor" to "data"))
        val sampler = FakeSampler(samplerTraceState = traceState)
        val tracer = buildTracer(sampler)
        val span = tracer.startSpan("test")
        assertEquals("data", span.spanContext.traceState.get("vendor"))
    }

    @Test
    fun testSamplerAttrsRespectLimits() {
        val cfg = fakeSpanLimitsConfig
        val limitedConfig = SpanLimitConfig(
            attributeCountLimit = 2,
            attributeValueLengthLimit = cfg.attributeValueLengthLimit,
            linkCountLimit = cfg.linkCountLimit,
            eventCountLimit = cfg.eventCountLimit,
            attributeCountPerEventLimit = cfg.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = cfg.attributeCountPerLinkLimit,
        )
        val sampler = FakeSampler(
            samplerAttributes = mapOf(
                "key1" to "value1",
                "key2" to "value2",
                "key3" to "value3",
            )
        )
        val tracer = buildTracer(sampler, limitedConfig)
        val span = tracer.startSpan("test")
        assertEquals(2, span.toReadableSpan().attributes.size)
    }

    @Test
    fun testSamplerReceiverUserAttributes() {
        val sampler = FakeSampler()
        val tracer = buildTracer(sampler)
        tracer.startSpan("test") {
            setStringAttribute("user.key", "user.value")
        }

        assertEquals("user.value", sampler.lastAttributes["user.key"])
    }

    @Test
    fun testSamplerReceivesDuplicateLinks() {
        val sampler = FakeSampler()
        val tracer = buildTracer(sampler)
        tracer.startSpan("test") {
            addLink(FakeSpanContext.VALID)
            addLink(FakeSpanContext.VALID)
        }
        assertEquals(2, sampler.lastLinks.size)
    }

    @Test
    fun testSamplerReceiversUserLinks() {
        val sampler = FakeSampler()
        val tracer = buildTracer(sampler)

        val ctxA = FakeSpanContext.INVALID
        val ctxB = FakeSpanContext.VALID

        tracer.startSpan("test") {
            addLink(ctxA)
            addLink(ctxB)
        }

        assertEquals(2, sampler.lastLinks.size)
        assertEquals(ctxA.spanId, sampler.lastLinks[0].spanContext.spanId)
        assertEquals(ctxB.spanId, sampler.lastLinks[1].spanContext.spanId)
    }

    @Test
    fun testSamplerReceiverCappedLinks() {
        val sampler = FakeSampler()

        val cfg = SpanLimitConfig(
            attributeCountLimit = fakeSpanLimitsConfig.attributeCountLimit,
            attributeValueLengthLimit = fakeSpanLimitsConfig.attributeValueLengthLimit,
            linkCountLimit = 1,
            eventCountLimit = fakeSpanLimitsConfig.eventCountLimit,
            attributeCountPerEventLimit = fakeSpanLimitsConfig.attributeCountPerEventLimit,
            attributeCountPerLinkLimit = fakeSpanLimitsConfig.attributeCountPerLinkLimit,
        )

        val tracer = buildTracer(sampler, cfg)
        val ctxA = FakeSpanContext.INVALID
        val ctxB = FakeSpanContext.VALID

        tracer.startSpan("test") {
            addLink(ctxA)
            addLink(ctxB)
        }

        assertEquals(1, sampler.lastLinks.size)
        assertEquals(ctxA.spanId, sampler.lastLinks[0].spanContext.spanId)
    }
}
