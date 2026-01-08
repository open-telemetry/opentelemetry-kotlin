package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.createCompatSdkFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanExtTest {

    private val factory = createCompatSdkFactory()
    private val generator = OtelJavaIdGenerator.random()

    private val validSpanContext = factory.spanContextFactory.create(
        traceId = generator.generateTraceId(),
        spanId = generator.generateSpanId(),
        traceState = factory.traceStateFactory.default,
        traceFlags = factory.traceFlagsFactory.default,
    )

    @Test
    fun `test invalid span`() {
        val invalid = factory.spanFactory.invalid
        assertSpanContextsMatch(factory.spanContextFactory.invalid, invalid.spanContext)
        assertSpanContextsMatch(factory.spanContextFactory.invalid, invalid.parent)
    }

    @Test
    fun `test from span context valid`() {
        val span = factory.spanFactory.fromSpanContext(validSpanContext)
        assertSpanContextsMatch(validSpanContext, span.spanContext)
        assertSpanContextsMatch(factory.spanContextFactory.invalid, span.parent)
    }

    @Test
    fun `test from span context invalid`() {
        val span = factory.spanFactory.fromSpanContext(factory.spanContextFactory.invalid)
        assertEquals(factory.spanFactory.invalid, span)
    }

    @Test
    fun `test from context invalid`() {
        val span = factory.spanFactory.fromContext(factory.contextFactory.root())
        assertSpanContextsMatch(factory.spanContextFactory.invalid, span.spanContext)
    }

    @Test
    fun `test from context valid`() {
        val spanContext = OtelJavaSpanContext.create(
            generator.generateTraceId(),
            generator.generateSpanId(),
            OtelJavaTraceFlags.getDefault(),
            OtelJavaTraceState.getDefault()
        )
        val span = SpanAdapter(
            OtelJavaSpan.wrap(spanContext),
            FakeClock(),
            OtelJavaContext.root(),
            SpanKind.INTERNAL,
            0,
            CompatSpanLimitsConfig(),
        )
        val root = factory.contextFactory.root()
        val ctx = span.storeInContext(root)
        val observed = factory.spanFactory.fromContext(root).spanContext
        assertSpanContextsMatch(factory.spanContextFactory.invalid, observed)

        val retrievedSpan = factory.spanFactory.fromContext(ctx)
        assertSpanContextsMatch(SpanContextAdapter(spanContext), retrievedSpan.spanContext)
    }
}
