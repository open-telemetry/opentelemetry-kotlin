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
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.tracing.SpanKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalApi::class)
internal class ProbabilitySamplerTest {

    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val contextFactory = ContextFactoryImpl()
    private val spanFactory = SpanFactoryImpl(spanContextFactory, contextFactory.spanKey)

    private fun createParentContext(traceId: String, otTraceStateValue: String): Context {
        val traceState = traceStateFactory.default.put("ot", otTraceStateValue)
        val parentSpanContext = spanContextFactory.create(
            traceIdBytes = traceId.hexToByteArray(),
            spanIdBytes = idGenerator.generateSpanIdBytes(),
            traceFlags = traceFlagsFactory.default,
            traceState = traceState,
            isRemote = true,
        )
        return contextFactory.storeSpan(
            contextFactory.root(),
            spanFactory.fromSpanContext(parentSpanContext),
        )
    }

    @Test
    fun testRecordsAndSamplesSpan() {
        val result = ProbabilitySampler(spanFactory, 0.5).shouldSample(
            context = contextFactory.root(),
            traceId = "000000000000000000ffffffffffffff",
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun testDropsSpan() {
        val result = ProbabilitySampler(spanFactory, 0.5).shouldSample(
            context = contextFactory.root(),
            traceId = "ffffffffffffffffff00000000000000",
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.DROP, result.decision)
    }

    @Test
    fun testRecordsAndSamplesSpanAtMinimumRatio() {
        val ratio = 1.0 / (1L shl 56).toDouble()
        val result = ProbabilitySampler(spanFactory, ratio).shouldSample(
            context = contextFactory.root(),
            traceId = "000000000000000000ffffffffffffff",
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun testDropsSpanAtMinimumRatio() {
        val ratio = 1.0 / (1L shl 56).toDouble()
        val result = ProbabilitySampler(spanFactory, ratio).shouldSample(
            context = contextFactory.root(),
            traceId = "000000000000000000fffffffffffffe",
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.DROP, result.decision)
    }

    @Test
    fun testThrowsOnInvalidRatio() {
        assertFailsWith(IllegalArgumentException::class) {
            ProbabilitySampler(spanFactory, 0.0)
        }
        assertFailsWith(IllegalArgumentException::class) {
            ProbabilitySampler(spanFactory, 2.0)
        }
    }

    @Test
    fun recordsAndSamplesSpanWithExplicitRandomness() {
        val traceId = "aaaaaaaaaaaaaaaaaa70000000000000"
        val context = createParentContext(traceId, otTraceStateValue = "rv:90000000000000")
        val result = ProbabilitySampler(spanFactory, 0.5).shouldSample(
            context = context,
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun fallsBackToTraceIdRandomness() {
        val traceId = "aaaaaaaaaaaaaaaaaa90000000000000"
        val context = createParentContext(traceId, otTraceStateValue = "rv:garbage")
        val result = ProbabilitySampler(spanFactory, 0.5).shouldSample(
            context = context,
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, result.decision)
    }

    @Test
    fun updatesExistingThreshold() {
        val traceId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        val context = createParentContext(traceId, otTraceStateValue = "th:123")
        val result = ProbabilitySampler(spanFactory, 0.5).shouldSample(
            context = context,
            traceId = traceId,
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals("th:8", result.traceState.get("ot"))
    }
}
