package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.factory.CompatSpanFactory
import io.opentelemetry.kotlin.init.CompatSpanLimitsConfig
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanExtTest {

    private val spanFactory = CompatSpanFactory()
    private val contextFactory = CompatContextFactory()
    private val generator = OtelJavaIdGenerator.random()

    private val validSpanContext = createSpanContext(generator.generateTraceId(), generator.generateSpanId())

    @Test
    fun `test invalid span`() {
        val invalid = spanFactory.invalid
        assertSpanContextsMatch(createInvalidSpanContext(), invalid.spanContext)
    }

    @Test
    fun `test from span context valid`() {
        val span = spanFactory.fromSpanContext(validSpanContext)
        assertSpanContextsMatch(validSpanContext, span.spanContext)
    }

    @Test
    fun `test from span context invalid`() {
        val span = spanFactory.fromSpanContext(createInvalidSpanContext())
        assertEquals(spanFactory.invalid, span)
    }

    @Test
    fun `test from context invalid`() {
        val span = contextFactory.root().extractSpan()
        assertSpanContextsMatch(createInvalidSpanContext(), span.spanContext)
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
            OtelJavaContext.root(),
            SpanKind.INTERNAL,
            CompatSpanLimitsConfig(),
            NoopSdkErrorHandler,
        )
        val root = contextFactory.root()
        val ctx = span.storeInContext(root)
        val observed = root.extractSpan().spanContext
        assertSpanContextsMatch(createInvalidSpanContext(), observed)

        val retrievedSpan = ctx.extractSpan()
        assertSpanContextsMatch(spanContext.toOtelKotlinSpanContext(), retrievedSpan.spanContext)
    }
}
