package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import kotlin.test.Test
import kotlin.test.assertFalse

internal class SpanExtTest {

    private val contextFactory = CompatContextFactory()
    private val generator = OtelJavaIdGenerator.random()

    private val validSpanContext = createSpanContext(generator.generateTraceId(), generator.generateSpanId())

    @Test
    fun `test store span context valid`() {
        val span = contextFactory.rootContext().storeSpanContext(validSpanContext).extractSpan()
        assertSpanContextsMatch(validSpanContext, span.spanContext)
    }

    @Test
    fun `test store span context invalid`() {
        val span = contextFactory.rootContext().storeSpanContext(createInvalidSpanContext()).extractSpan()
        assertFalse(span.spanContext.isValid)
    }

    @Test
    fun `test from context invalid`() {
        val span = contextFactory.rootContext().extractSpan()
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
            NoopSdkErrorHandler,
        )
        val root = contextFactory.rootContext()
        val ctx = span.storeInContext(root)
        val observed = root.extractSpan().spanContext
        assertSpanContextsMatch(createInvalidSpanContext(), observed)

        val retrievedSpan = ctx.extractSpan()
        assertSpanContextsMatch(spanContext.toOtelKotlinSpanContext(), retrievedSpan.spanContext)
    }

    @Test
    fun `test store span context round trip`() {
        val root = contextFactory.rootContext()
        val ctx = root.storeSpanContext(validSpanContext)
        assertSpanContextsMatch(validSpanContext, ctx.extractSpan().spanContext)
        assertSpanContextsMatch(createInvalidSpanContext(), root.extractSpan().spanContext)
    }

    @Test
    fun `test store invalid span context`() {
        val ctx = contextFactory.rootContext().storeSpanContext(createInvalidSpanContext())
        assertSpanContextsMatch(createInvalidSpanContext(), ctx.extractSpan().spanContext)
    }
}
