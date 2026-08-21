package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.init.SamplerConfigDsl
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
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

    private fun Sampler.sample(
        context: Context = contextFactory.root(),
        traceId: String = this@BuiltInCompositeSamplerTest.traceId,
    ): SamplingResult =
        shouldSample(context, traceId.hexToByteArray(), "span", SpanKind.INTERNAL, AttributesModel(), emptyList())

    private fun ComposableSampler.intent(context: Context = contextFactory.root()): SamplingIntent =
        getSamplingIntent(context, "span", SpanKind.INTERNAL, AttributesModel(), emptyList())

    private fun fakeComposableSampler(intent: SamplingIntent): ComposableSampler =
        object : ComposableSampler {
            override fun getSamplingIntent(
                context: Context,
                name: String,
                spanKind: SpanKind,
                attributes: AttributeContainer,
                links: List<SpanLink>
            ) = intent

            override val description = "Fake"
        }

    @Test
    fun `always samples when delegate is composableAlwaysOn`() {
        val result = samplerDsl.composite { composableAlwaysOn() }.sample()
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:0", result.traceState.get("ot"))
        assertTrue(result.attributes.attributes.isEmpty())
    }

    @Test
    fun `never samples when delegate is composableAlwaysOff`() {
        val result = samplerDsl.composite { composableAlwaysOff() }.sample()
        assertEquals(SamplingResult.Decision.DROP, result.decision)
        assertNull(result.traceState.get("ot"))
        assertTrue(result.attributes.attributes.isEmpty())
    }

    @Test
    fun `composableProbability with ratio 0 behaves like composableAlwaysOff`() {
        val result = samplerDsl.composite { composableProbability(0.0) }.sample()
        assertEquals(SamplingResult.Decision.DROP, result.decision)
    }

    @Test
    fun `composableProbability reports adjustedCountReliable true per spec`() {
        assertTrue(samplerDsl.composableProbability(0.5).intent().adjustedCountReliable)
    }

    @Test
    fun `always samples when delegate is composableProbability at ratio 1 and publishes threshold`() {
        val result = samplerDsl.composite { composableProbability(1.0) }.sample()
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:0", result.traceState.get("ot"))
    }

    @Test
    fun `samples using trace id derived randomness when delegate is composableProbability`() {
        val result = samplerDsl.composite { composableProbability(0.5) }
            .sample(traceId = "000000000000000000ffffffffffffff")
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:8", result.traceState.get("ot"))
    }

    @Test
    fun `drops using trace id derived randomness when delegate is composableProbability`() {
        val result = samplerDsl.composite { composableProbability(0.5) }
            .sample(traceId = "ffffffffffffffffff00000000000000")
        assertEquals(SamplingResult.Decision.DROP, result.decision)
        assertNull(result.traceState.get("ot"))
    }

    @Test
    fun `rewrites stale parent threshold when delegate is composableProbability`() {
        val result = samplerDsl.composite { composableProbability(0.5) }.sample(
            context = contextWithParent(sampled = true, isRemote = true, otValue = "th:123"),
            traceId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        )
        assertEquals("th:8", result.traceState.get("ot"))
    }

    @Test
    fun `decision matches injected randomness when delegate reports adjustedCountReliable false`() {
        val seed = 42L
        val threshold = thresholdFromRatio(0.5)
        val expectedRandomValue = Random(seed).nextLong() and 0x00FFFFFFFFFFFFFFL
        val expectedDecision = if (threshold <= expectedRandomValue) {
            SamplingResult.Decision.RECORD_AND_SAMPLE
        } else {
            SamplingResult.Decision.DROP
        }

        val unreliableDelegate = fakeComposableSampler(
            SamplingIntentImpl(threshold = threshold, adjustedCountReliable = false)
        )
        val result = CompositeSampler(unreliableDelegate, random = Random(seed)).sample()

        assertEquals(expectedDecision, result.decision)
        assertNull(result.traceState.get("ot"))
    }

    @Test
    fun `delegates to root when delegate is composableParentThreshold and there is no valid parent`() {
        val result = samplerDsl.composite { composableParentThreshold(root = composableAlwaysOn()) }.sample()
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun `propagates parent threshold when delegate is composableParentThreshold`() {
        val result = samplerDsl.composite { composableParentThreshold(root = composableAlwaysOff()) }
            .sample(context = contextWithParent(sampled = true, isRemote = true, otValue = "th:8"))
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:8", result.traceState.get("ot"))
    }

    @Test
    fun `does not publish threshold when parent sampled without a threshold via composableParentThreshold`() {
        val result = samplerDsl.composite { composableParentThreshold(root = composableAlwaysOff()) }
            .sample(context = contextWithParent(sampled = true, isRemote = true))
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertNull(result.traceState.get("ot"))
    }

    @Test
    fun `drops when parent not sampled without a threshold via composableParentThreshold`() {
        val result = samplerDsl.composite { composableParentThreshold(root = composableAlwaysOn()) }
            .sample(context = contextWithParent(sampled = false, isRemote = true))
        assertEquals(SamplingResult.Decision.DROP, result.decision)
        assertNull(result.traceState.get("ot"))
    }

    @Test
    fun `annotates sampled spans via composableAnnotating`() {
        val annotating = samplerDsl.composableAnnotating(samplerDsl.composableProbability(0.5)) {
            setStringAttribute("sampling.rule", "half")
            setLongAttribute("sampling.priority", 1)
        }
        val result = samplerDsl.composite { annotating }.sample()

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:8", result.traceState.get("ot"))
        assertEquals(mapOf("sampling.rule" to "half", "sampling.priority" to 1L), result.attributes.attributes)
        assertTrue(annotating.intent().adjustedCountReliable)
    }

    @Test
    fun `does not annotate dropped spans via composableAnnotating`() {
        val result = samplerDsl.composite {
            composableAnnotating(composableAlwaysOff()) { setStringAttribute("sampling.rule", "off") }
        }.sample()
        assertEquals(SamplingResult.Decision.DROP, result.decision)
        assertTrue(result.attributes.attributes.isEmpty())
    }

    @Test
    fun `merges delegate attributes via composableAnnotating`() {
        val traceStateProvider: (TraceState, SamplingResult.Decision) -> TraceState =
            { traceState, _ -> traceState.put("vendor", "value") }
        val delegate = fakeComposableSampler(
            SamplingIntentImpl(
                threshold = 0,
                adjustedCountReliable = true,
                attributesProvider = {
                    AttributesModel().apply {
                        setStringAttribute("sampling.rule", "from-delegate")
                        setStringAttribute("delegate.only", "kept")
                    }
                },
                traceStateProvider = traceStateProvider,
            )
        )
        val annotating = ComposableAnnotatingSampler(delegate) {
            setStringAttribute("sampling.rule", "from-annotation")
        }
        val result = CompositeSampler(annotating).sample()

        assertEquals(
            mapOf("sampling.rule" to "from-annotation", "delegate.only" to "kept"),
            result.attributes.attributes,
        )
        assertSame(traceStateProvider, annotating.intent().traceStateProvider)
    }
}
