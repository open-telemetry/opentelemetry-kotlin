package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.factory.CompatTraceFlagsFactory
import io.opentelemetry.kotlin.factory.CompatTraceStateFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter
import io.opentelemetry.kotlin.tracing.model.SpanKind
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanExtTest {

    private val spanContextFactory = CompatSpanContextFactory()
    private val traceStateFactory = CompatTraceStateFactory()
    private val traceFlagsFactory = CompatTraceFlagsFactory()
    private val spanFactory = CompatSpanFactory(spanContextFactory)
    private val contextFactory = CompatContextFactory()
    private val generator = OtelJavaIdGenerator.random()

    private val validSpanContext = spanContextFactory.create(
        traceId = generator.generateTraceId(),
        spanId = generator.generateSpanId(),
        traceState = traceStateFactory.default,
        traceFlags = traceFlagsFactory.default,
    )

    @Test
    fun `test invalid span`() {
        val invalid = spanFactory.invalid
        assertSpanContextsMatch(spanContextFactory.invalid, invalid.spanContext)
        assertSpanContextsMatch(spanContextFactory.invalid, invalid.parent)
    }

    @Test
    fun `test from span context valid`() {
        val span = spanFactory.fromSpanContext(validSpanContext)
        assertSpanContextsMatch(validSpanContext, span.spanContext)
        assertSpanContextsMatch(spanContextFactory.invalid, span.parent)
    }

    @Test
    fun `test from span context invalid`() {
        val span = spanFactory.fromSpanContext(spanContextFactory.invalid)
        assertEquals(spanFactory.invalid, span)
    }

    @Test
    fun `test from context invalid`() {
        val span = spanFactory.fromContext(contextFactory.root())
        assertSpanContextsMatch(spanContextFactory.invalid, span.spanContext)
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
        val root = contextFactory.root()
        val ctx = span.storeInContext(root)
        val observed = spanFactory.fromContext(root).spanContext
        assertSpanContextsMatch(spanContextFactory.invalid, observed)

        val retrievedSpan = spanFactory.fromContext(ctx)
        assertSpanContextsMatch(SpanContextAdapter(spanContext), retrievedSpan.spanContext)
    }
}
