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
import io.opentelemetry.kotlin.tracing.SpanLinkImpl
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import io.opentelemetry.kotlin.tracing.model.SpanLink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class ComposableRuleBasedSamplerTest {

    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)

    private val samplerDsl = object : SamplerConfigDsl {
        override val spanFactory = this@ComposableRuleBasedSamplerTest.spanFactory
    }

    private val traceId = "000000000000000000ffffffffffffff"

    private val matchesEverything = SamplingPredicate { _, _, _, _, _ -> true }
    private val matchesNothing = SamplingPredicate { _, _, _, _, _ -> false }

    @Test
    fun `first matching rule wins when several rules match`() {
        val sampler = samplerDsl.composableRuleBased {
            rule(matchesEverything) { composableAlwaysOn() }
            rule(matchesEverything) { composableProbability(0.5) }
        }

        val intent = sampler.intentFor()
        assertEquals(0L, intent.threshold)
        assertTrue(intent.adjustedCountReliable)
    }

    @Test
    fun `later predicates are not evaluated once a rule matches`() {
        var laterPredicateEvaluated = false
        val sampler = samplerDsl.composableRuleBased {
            rule(matchesEverything) { composableAlwaysOn() }
            rule({ _, _, _, _, _ ->
                laterPredicateEvaluated = true
                true
            }) { composableAlwaysOff() }
        }

        sampler.intentFor()
        assertFalse(laterPredicateEvaluated)
    }

    @Test
    fun `returns non sampling intent when no rule matches`() {
        val sampler = samplerDsl.composableRuleBased {
            rule(matchesNothing) { composableAlwaysOn() }
        }

        val intent = sampler.intentFor()
        assertNull(intent.threshold)
        assertFalse(intent.adjustedCountReliable)
    }

    @Test
    fun `returns non sampling intent when there are no rules`() {
        val intent = samplerDsl.composableRuleBased { }.intentFor()
        assertNull(intent.threshold)
        assertFalse(intent.adjustedCountReliable)
    }

    @Test
    fun `drops span through composite when no rule matches`() {
        val result = samplerDsl.composite {
            composableRuleBased {
                rule({ _, name, _, _, _ -> name == "other" }) { composableAlwaysOn() }
            }
        }.shouldSample(
            context = contextFactory.root(),
            traceIdBytes = traceId.hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )

        assertEquals(SamplingResult.Decision.DROP, result.decision)
        assertNull(result.traceState.get("ot"))
    }

    @Test
    fun `samples span through composite when a rule matches`() {
        val result = samplerDsl.composite {
            composableRuleBased {
                rule({ _, name, _, _, _ -> name == "span" }) { composableAlwaysOn() }
            }
        }.shouldSample(
            context = contextFactory.root(),
            traceIdBytes = traceId.hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:0", result.traceState.get("ot"))
    }

    @Test
    fun `matches on span name and span kind and falls through to the last rule`() {
        val sampler = samplerDsl.composableRuleBased {
            rule({ _, name, _, _, _ -> name == "/health" }) { composableAlwaysOff() }
            rule({ _, _, spanKind, _, _ -> spanKind == SpanKind.SERVER }) { composableAlwaysOn() }
            rule(matchesEverything) { composableProbability(0.1) }
        }

        assertNull(sampler.intentFor(name = "/health", spanKind = SpanKind.SERVER).threshold)
        assertEquals(0L, sampler.intentFor(name = "/checkout", spanKind = SpanKind.SERVER).threshold)
        assertEquals(
            thresholdFromRatio(0.1),
            sampler.intentFor(name = "/checkout", spanKind = SpanKind.CLIENT).threshold,
        )
    }

    @Test
    fun `predicate receives the span creation parameters`() {
        var observedContext: Context? = null
        var observedName: String? = null
        var observedSpanKind: SpanKind? = null
        var observedAttributes: AttributeContainer? = null
        var observedLinks: List<SpanLink>? = null

        val sampler = samplerDsl.composableRuleBased {
            rule({ context, name, spanKind, attributes, links ->
                observedContext = context
                observedName = name
                observedSpanKind = spanKind
                observedAttributes = attributes
                observedLinks = links
                true
            }) { composableAlwaysOn() }
        }

        val context = contextWithParent(sampled = true, isRemote = true)
        val attributes = AttributesModel().apply { setStringAttribute("http.route", "/checkout") }
        val links = listOf(SpanLinkImpl(spanContextFactory.invalid, AttributesModel()))
        sampler.intentFor(
            context = context,
            name = "checkout",
            spanKind = SpanKind.CLIENT,
            attributes = attributes,
            links = links,
        )

        assertSame(context, observedContext)
        assertEquals("checkout", observedName)
        assertEquals(SpanKind.CLIENT, observedSpanKind)
        assertEquals("/checkout", observedAttributes?.attributes?.get("http.route"))
        assertEquals(links, observedLinks)
    }

    @Test
    fun `sampler blocks are evaluated once at configuration time`() {
        var samplersBuilt = 0
        val sampler = samplerDsl.composableRuleBased {
            rule(matchesEverything) {
                samplersBuilt++
                composableAlwaysOn()
            }
        }

        sampler.intentFor()
        sampler.intentFor()
        assertEquals(1, samplersBuilt)
    }

    @Test
    fun `nests a rule based sampler inside a rule`() {
        val sampler = samplerDsl.composableRuleBased {
            rule({ _, _, spanKind, _, _ -> spanKind == SpanKind.SERVER }) {
                composableRuleBased {
                    rule({ _, name, _, _, _ -> name == "/health" }) { composableAlwaysOff() }
                    rule(matchesEverything) { composableAlwaysOn() }
                }
            }
        }

        assertNull(sampler.intentFor(name = "/health", spanKind = SpanKind.SERVER).threshold)
        assertEquals(0L, sampler.intentFor(name = "/checkout", spanKind = SpanKind.SERVER).threshold)
        assertNull(sampler.intentFor(name = "/checkout", spanKind = SpanKind.CLIENT).threshold)
    }

    @Test
    fun `composes with composableParentThreshold`() {
        val result = samplerDsl.composite {
            composableRuleBased {
                rule({ _, name, _, _, _ -> name == "span" }) {
                    composableParentThreshold(root = composableAlwaysOff())
                }
            }
        }.shouldSample(
            context = contextWithParent(sampled = true, isRemote = true, otValue = "th:8"),
            traceIdBytes = traceId.hexToByteArray(),
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
        assertEquals("th:8", result.traceState.get("ot"))
    }

    @Test
    fun `description lists rule samplers in order`() {
        val sampler = samplerDsl.composableRuleBased {
            rule(matchesNothing) { composableAlwaysOff() }
            rule(matchesNothing) { composableAlwaysOn() }
            rule(matchesEverything) { composableProbability(0.1) }
        }

        assertEquals(
            "ComposableRuleBasedSampler{rules:[ComposableAlwaysOffSampler,ComposableAlwaysOnSampler," +
                "ComposableProbabilitySampler{0.1}]}",
            sampler.description,
        )
    }

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

    private fun ComposableSampler.intentFor(
        context: Context = contextFactory.root(),
        name: String = "span",
        spanKind: SpanKind = SpanKind.INTERNAL,
        attributes: AttributeContainer = AttributesModel(),
        links: List<SpanLink> = emptyList(),
    ): SamplingIntent = getSamplingIntent(context, name, spanKind, attributes, links)
}
