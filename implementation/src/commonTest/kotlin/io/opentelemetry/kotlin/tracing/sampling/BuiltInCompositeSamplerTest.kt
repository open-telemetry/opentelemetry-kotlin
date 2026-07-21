package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.SamplerConfigDsl
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BuiltInCompositeSamplerTest {

    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)

    private val samplerDsl = object : SamplerConfigDsl {
        override val spanFactory = this@BuiltInCompositeSamplerTest.spanFactory
    }

    private val traceId = "000000000000000000ffffffffffffff"

    private fun contextWithParent(sampled: Boolean, isRemote: Boolean, otValue: String? = null): Context {
        val traceFlags = if (sampled) {
            traceFlagsFactory.default
        } else {
            TraceFlagsImpl(isSampled = false, isRandom = false)
        }
        val traceState = otValue?.let { traceStateFactory.default.put("ot", it) } ?: traceStateFactory.default
        val parentSpanContext = spanContextFactory.create(
            traceId = "12345678901234567890123456789012",
            spanId = "1234567890123456",
            traceFlags = traceFlags,
            traceState = traceState,
            isRemote = isRemote,
        )
        val parentSpan = NonRecordingSpan(spanContextFactory.invalid, parentSpanContext)
        return contextFactory.root().storeSpan(parentSpan)
    }

    @Test
    fun `always samples when delegate is composableAlwaysOn`() {
        val result = samplerDsl.composite(samplerDsl.composableAlwaysOn()).shouldSample(
            context = contextFactory.root(),
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:0", result.traceState.get("ot"))
        assertTrue(result.attributes.attributes.isEmpty())
    }

    @Test
    fun `never samples when delegate is composableAlwaysOff`() {
        val result = samplerDsl.composite(samplerDsl.composableAlwaysOff()).shouldSample(
            context = contextFactory.root(),
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.DROP, result.decision)
        assertNull(result.traceState.get("ot"))
        assertTrue(result.attributes.attributes.isEmpty())
    }

    @Test
    fun `composableProbability with ratio 0 behaves like composableAlwaysOff`() {
        val result = samplerDsl.composite(samplerDsl.composableProbability(0.0)).shouldSample(
            context = contextFactory.root(),
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.DROP, result.decision)
    }

    @Test
    fun `always samples when delegate is composableProbability at ratio 1 without publishing threshold`() {
        val result = samplerDsl.composite(samplerDsl.composableProbability(1.0)).shouldSample(
            context = contextFactory.root(),
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertNull(result.traceState.get("ot"))
    }

    @Test
    fun `decision matches injected randomness when delegate is composableProbability`() {
        val seed = 42L
        val ratio = 0.5
        val expectedRandomValue = Random(seed).nextLong() and 0x00FFFFFFFFFFFFFFL
        val expectedDecision = if (thresholdFromRatio(ratio) <= expectedRandomValue) {
            SamplingResult.Decision.RECORD_AND_SAMPLE
        } else {
            SamplingResult.Decision.DROP
        }

        val sampler = CompositeSampler(samplerDsl.composableProbability(ratio), random = Random(seed))
        val result = sampler.shouldSample(
            context = contextFactory.root(),
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(expectedDecision, result.decision)
    }

    @Test
    fun `delegates to root when delegate is composableParentThreshold and there is no valid parent`() {
        val delegate = samplerDsl.composableParentThreshold(root = samplerDsl.composableAlwaysOn())
        val result = samplerDsl.composite(delegate).shouldSample(
            context = contextFactory.root(),
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun `propagates parent threshold when delegate is composableParentThreshold`() {
        val context = contextWithParent(sampled = true, isRemote = true, otValue = "th:8")
        val delegate = samplerDsl.composableParentThreshold(root = samplerDsl.composableAlwaysOff())
        val result = samplerDsl.composite(delegate).shouldSample(
            context = context,
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:8", result.traceState.get("ot"))
    }

    @Test
    fun `does not publish threshold when parent sampled without a threshold via composableParentThreshold`() {
        val context = contextWithParent(sampled = true, isRemote = true)
        val delegate = samplerDsl.composableParentThreshold(root = samplerDsl.composableAlwaysOff())
        val result = samplerDsl.composite(delegate).shouldSample(
            context = context,
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertNull(result.traceState.get("ot"))
    }

    @Test
    fun `drops when parent not sampled without a threshold via composableParentThreshold`() {
        val context = contextWithParent(sampled = false, isRemote = true)
        val delegate = samplerDsl.composableParentThreshold(root = samplerDsl.composableAlwaysOn())
        val result = samplerDsl.composite(delegate).shouldSample(
            context = context,
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.DROP, result.decision)
        assertNull(result.traceState.get("ot"))
    }
}
