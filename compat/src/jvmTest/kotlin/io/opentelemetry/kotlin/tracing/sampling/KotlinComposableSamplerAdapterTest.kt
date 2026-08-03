package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanKind
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val NEVER_SAMPLE_THRESHOLD: Long = 1L shl 56

@OptIn(ExperimentalApi::class)
internal class KotlinComposableSamplerAdapterTest {

    @Test
    fun `description delegates to the kotlin sampler`() {
        val delegate = FakeComposableSampler(description = "custom description")
        val adapter = KotlinComposableSamplerAdapter(delegate)

        assertEquals("custom description", adapter.getDescription())
    }

    @Test
    fun `null threshold maps to the never sample sentinel`() {
        val delegate = FakeComposableSampler(intent = FakeSamplingIntent(threshold = null))
        val adapter = KotlinComposableSamplerAdapter(delegate)

        val intent = adapter.getSamplingIntent(
            OtelJavaContext.root(),
            "trace-id",
            "span",
            OtelJavaSpanKind.CLIENT,
            OtelJavaAttributes.empty(),
            emptyList(),
        )

        assertEquals(NEVER_SAMPLE_THRESHOLD, intent.threshold)
    }

    @Test
    fun `non null threshold and reliability are preserved`() {
        val delegate = FakeComposableSampler(intent = FakeSamplingIntent(threshold = 7L, adjustedCountReliable = false))
        val adapter = KotlinComposableSamplerAdapter(delegate)

        val intent = adapter.getSamplingIntent(
            OtelJavaContext.root(),
            "trace-id",
            "span",
            OtelJavaSpanKind.CLIENT,
            OtelJavaAttributes.empty(),
            emptyList(),
        )

        assertEquals(7L, intent.threshold)
        assertFalse(intent.isThresholdReliable)
    }

    @Test
    fun `missing attributes provider yields empty java attributes`() {
        val delegate = FakeComposableSampler(intent = FakeSamplingIntent(attributesProvider = null))
        val adapter = KotlinComposableSamplerAdapter(delegate)

        val intent = adapter.getSamplingIntent(
            OtelJavaContext.root(),
            "trace-id",
            "span",
            OtelJavaSpanKind.CLIENT,
            OtelJavaAttributes.empty(),
            emptyList(),
        )

        assertTrue(intent.attributes.isEmpty)
    }

    @Test
    fun `non compat attribute container from provider yields empty java attributes`() {
        val nonCompatAttributes = object : AttributeContainer {
            override val attributes: Map<String, Any> = mapOf("foo" to "bar")
        }
        val delegate = FakeComposableSampler(
            intent = FakeSamplingIntent(attributesProvider = { nonCompatAttributes }),
        )
        val adapter = KotlinComposableSamplerAdapter(delegate)

        val intent = adapter.getSamplingIntent(
            OtelJavaContext.root(),
            "trace-id",
            "span",
            OtelJavaSpanKind.CLIENT,
            OtelJavaAttributes.empty(),
            emptyList(),
        )

        assertTrue(intent.attributes.isEmpty)
    }

    @Test
    fun `compat attribute container from provider is forwarded`() {
        val compatAttributes = CompatAttributesModel()
        compatAttributes.setStringAttribute("foo", "bar")
        val delegate = FakeComposableSampler(
            intent = FakeSamplingIntent(attributesProvider = { compatAttributes }),
        )
        val adapter = KotlinComposableSamplerAdapter(delegate)

        val intent = adapter.getSamplingIntent(
            OtelJavaContext.root(),
            "trace-id",
            "span",
            OtelJavaSpanKind.CLIENT,
            OtelJavaAttributes.empty(),
            emptyList(),
        )

        assertEquals("bar", intent.attributes.get(OtelJavaAttributeKey.stringKey("foo")))
    }

    @Test
    fun `missing trace state provider yields an identity updater`() {
        val delegate = FakeComposableSampler(intent = FakeSamplingIntent(traceStateProvider = null))
        val adapter = KotlinComposableSamplerAdapter(delegate)

        val intent = adapter.getSamplingIntent(
            OtelJavaContext.root(),
            "trace-id",
            "span",
            OtelJavaSpanKind.CLIENT,
            OtelJavaAttributes.empty(),
            emptyList(),
        )
        val defaultTraceState = OtelJavaTraceState.getDefault()

        assertEquals(defaultTraceState, intent.traceStateUpdater?.apply(defaultTraceState))
    }

    @Test
    fun `trace state provider is applied through the updater`() {
        var observedTraceState: TraceState? = null
        var observedDecision: SamplingResult.Decision? = null
        val delegate = FakeComposableSampler(
            intent = FakeSamplingIntent(
                traceStateProvider = { traceState, decision ->
                    observedTraceState = traceState
                    observedDecision = decision
                    traceState.put("th", "1")
                },
            ),
        )
        val adapter = KotlinComposableSamplerAdapter(delegate)

        val intent = adapter.getSamplingIntent(
            OtelJavaContext.root(),
            "trace-id",
            "span",
            OtelJavaSpanKind.CLIENT,
            OtelJavaAttributes.empty(),
            emptyList(),
        )
        val result = intent.traceStateUpdater?.apply(OtelJavaTraceState.getDefault())

        assertEquals("1", result?.get("th"))
        assertEquals(SamplingResult.Decision.RECORD_AND_SAMPLE, observedDecision)
        assertEquals(emptyMap(), observedTraceState?.asMap())
    }

    @Test
    fun `context name and span kind are forwarded to the delegate`() {
        val delegate = FakeComposableSampler()
        val adapter = KotlinComposableSamplerAdapter(delegate)
        val context = OtelJavaContext.root()

        adapter.getSamplingIntent(
            context,
            "trace-id",
            "my-span",
            OtelJavaSpanKind.SERVER,
            OtelJavaAttributes.empty(),
            emptyList(),
        )

        assertEquals("my-span", delegate.lastName)
        assertEquals(OtelJavaSpanKind.SERVER.toOtelKotlinSpanKind(), delegate.lastSpanKind)
    }
}
