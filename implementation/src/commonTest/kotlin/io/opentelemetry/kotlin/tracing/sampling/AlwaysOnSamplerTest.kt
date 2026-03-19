package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult.Decision
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AlwaysOnSamplerTest {

    private val spanFactory = FakeSpanFactory()
    private val sampler = AlwaysOnSampler(spanFactory)
    private val context = FakeContext()

    @Test
    fun testSample() {
        val sample = sampler.shouldSample(
            context = context,
            traceId = "traceid",
            name = "span",
            spanKind = SpanKind.INTERNAL,
            attributes = AttributesModel(),
            links = emptyList(),
        )
        assertEquals(Decision.RECORD_AND_SAMPLE, sample.decision)
        assertTrue(sample.attributes.attributes.isEmpty())
        assertEquals("AlwaysOnSampler", sampler.description)

        val expected = spanFactory.fromContext(context).spanContext.traceState
        assertEquals(expected.asMap(), sample.traceState.asMap())
    }
}
