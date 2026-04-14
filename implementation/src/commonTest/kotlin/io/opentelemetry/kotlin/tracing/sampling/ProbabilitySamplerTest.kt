package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.tracing.SpanKind
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ProbabilitySamplerTest {

    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val contextFactory = ContextFactoryImpl()
    private val spanFactory = SpanFactoryImpl(spanContextFactory, contextFactory.spanKey)

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
}

