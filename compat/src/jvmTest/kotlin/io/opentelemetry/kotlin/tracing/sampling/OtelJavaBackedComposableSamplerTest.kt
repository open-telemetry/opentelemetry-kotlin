package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaComposableSampler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSamplingIntent
import io.opentelemetry.kotlin.tracing.FakeTraceState
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import org.junit.Test
import java.util.function.Function
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaBackedComposableSamplerTest {

    @Test
    fun `description delegates to impl`() {
        val impl = FakeOtelJavaComposableSampler()
        val wrapper = OtelJavaBackedComposableSampler(impl)

        assertEquals(impl.getDescription(), wrapper.description)
    }

    @Test
    fun `valid threshold and reliable count are preserved`() {
        val impl = FakeOtelJavaComposableSampler(
            intent = FakeOtelJavaSamplingIntent(threshold = 42L, thresholdReliable = true),
        )
        val wrapper = OtelJavaBackedComposableSampler(impl)

        val intent = wrapper.getSamplingIntent(FakeContext(), "span", SpanKind.CLIENT, CompatAttributesModel(), emptyList())

        assertEquals(42L, intent.threshold)
        assertTrue(intent.adjustedCountReliable)
    }

    @Test
    fun `threshold outside valid range maps to null`() {
        val impl = FakeOtelJavaComposableSampler(
            intent = FakeOtelJavaSamplingIntent(threshold = 1L shl 56, thresholdReliable = false),
        )
        val wrapper = OtelJavaBackedComposableSampler(impl)

        val intent = wrapper.getSamplingIntent(FakeContext(), "span", SpanKind.CLIENT, CompatAttributesModel(), emptyList())

        assertNull(intent.threshold)
        assertFalse(intent.adjustedCountReliable)
    }

    @Test
    fun `empty java attributes yield null attributesProvider`() {
        val impl = FakeOtelJavaComposableSampler(
            intent = FakeOtelJavaSamplingIntent(attributes = OtelJavaAttributes.empty()),
        )
        val wrapper = OtelJavaBackedComposableSampler(impl)

        val intent = wrapper.getSamplingIntent(FakeContext(), "span", SpanKind.CLIENT, CompatAttributesModel(), emptyList())

        assertNull(intent.attributesProvider)
    }

    @Test
    fun `non-empty java attributes are exposed via attributesProvider`() {
        val javaAttrs = OtelJavaAttributes.builder().put("foo", "bar").build()
        val impl = FakeOtelJavaComposableSampler(intent = FakeOtelJavaSamplingIntent(attributes = javaAttrs))
        val wrapper = OtelJavaBackedComposableSampler(impl)

        val intent = wrapper.getSamplingIntent(FakeContext(), "span", SpanKind.CLIENT, CompatAttributesModel(), emptyList())

        assertEquals(mapOf("foo" to "bar"), intent.attributesProvider?.invoke()?.attributes)
    }

    @Test
    fun `null trace state updater yields null traceStateProvider`() {
        val impl = FakeOtelJavaComposableSampler(intent = FakeOtelJavaSamplingIntent(traceStateUpdater = null))
        val wrapper = OtelJavaBackedComposableSampler(impl)

        val intent = wrapper.getSamplingIntent(FakeContext(), "span", SpanKind.CLIENT, CompatAttributesModel(), emptyList())

        assertNull(intent.traceStateProvider)
    }

    @Test
    fun `trace state updater is applied through the provider`() {
        val updater = Function<OtelJavaTraceState, OtelJavaTraceState> {
            it.toBuilder().put("th", "1").build()
        }
        val impl = FakeOtelJavaComposableSampler(intent = FakeOtelJavaSamplingIntent(traceStateUpdater = updater))
        val wrapper = OtelJavaBackedComposableSampler(impl)

        val intent = wrapper.getSamplingIntent(FakeContext(), "span", SpanKind.CLIENT, CompatAttributesModel(), emptyList())

        val result = intent.traceStateProvider?.invoke(FakeTraceState(emptyMap()), SamplingResult.Decision.RECORD_AND_SAMPLE)
        assertEquals("1", result?.get("th"))
    }

    @Test
    fun `non compat attribute containers are forwarded as empty attributes`() {
        val impl = FakeOtelJavaComposableSampler()
        val wrapper = OtelJavaBackedComposableSampler(impl)
        val nonCompatAttributes = object : AttributeContainer {
            override val attributes: Map<String, Any> = mapOf("foo" to "bar")
        }

        wrapper.getSamplingIntent(FakeContext(), "span", SpanKind.CLIENT, nonCompatAttributes, emptyList())

        assertTrue(impl.lastAttributes!!.isEmpty)
    }

    @Test
    fun `traceId name and spanKind are forwarded to impl`() {
        val impl = FakeOtelJavaComposableSampler()
        val wrapper = OtelJavaBackedComposableSampler(impl)
        val context = FakeContext()

        wrapper.getSamplingIntent(context, "my-span", SpanKind.SERVER, CompatAttributesModel(), emptyList())

        assertEquals(context.extractSpan().spanContext.traceId, impl.lastTraceId)
        assertEquals("my-span", impl.lastName)
        assertEquals(SpanKind.SERVER.toOtelJavaSpanKind(), impl.lastSpanKind)
    }
}
