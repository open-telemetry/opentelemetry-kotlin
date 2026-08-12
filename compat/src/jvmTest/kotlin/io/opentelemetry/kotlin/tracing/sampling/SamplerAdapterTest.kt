package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaSampler
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingDecision
import io.opentelemetry.kotlin.aliases.OtelJavaSamplingResult
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.attributes.EmptyAttributeContainer
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.tracing.SpanKind
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class SamplerAdapterTest {

    @Test
    fun `empty java attributes yield the shared empty container`() {
        val adapter = SamplerAdapter(javaSampler(OtelJavaAttributes.empty()))

        val first = adapter.sample()
        val second = adapter.sample()

        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, first.decision)
        assertSame(EmptyAttributeContainer, first.attributes)
        assertSame(first.attributes, second.attributes)
    }

    @Test
    fun `non-empty java attributes are surfaced`() {
        val javaAttributes = OtelJavaAttributes.builder().put("key", "value").build()
        val adapter = SamplerAdapter(javaSampler(javaAttributes))

        val result = adapter.sample()

        assertIs<CompatAttributesModel>(result.attributes)
        assertEquals(mapOf("key" to "value"), result.attributes.attributes)
    }

    private fun SamplerAdapter.sample(): SamplingResult = shouldSample(
        context = FakeContext(),
        traceId = "000000000000000000ffffffffffffff",
        name = "span",
        spanKind = SpanKind.INTERNAL,
        attributes = CompatAttributesModel(),
        links = emptyList(),
    )

    private fun javaSampler(samplerAttributes: OtelJavaAttributes) = object : OtelJavaSampler {
        override fun shouldSample(
            parentContext: OtelJavaContext,
            traceId: String,
            name: String,
            spanKind: OtelJavaSpanKind,
            attributes: OtelJavaAttributes,
            parentLinks: List<OtelJavaLinkData>,
        ): OtelJavaSamplingResult = OtelJavaSamplingResult.create(
            OtelJavaSamplingDecision.RECORD_AND_SAMPLE,
            samplerAttributes,
        )

        override fun getDescription(): String = "FakeOtelJavaSampler"
    }
}
