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
import io.opentelemetry.kotlin.tracing.model.SpanAdapter
import kotlin.test.Test
import kotlin.test.assertFalse

internal class SpanExtTest {

    private val generator = OtelJavaIdGenerator.random()

    private val validSpanContext = createSpanContext(generator.generateTraceId(), generator.generateSpanId())

    @Test
    fun `test store span context valid`() {
        val span = rootContext().storeSpanContext(validSpanContext).span
        assertSpanContextsMatch(validSpanContext, span.spanContext)
    }

    @Test
    fun `test store span context invalid`() {
        val span = rootContext().storeSpanContext(createInvalidSpanContext()).span
        assertFalse(span.spanContext.isValid)
    }

    @Test
    fun `test from context invalid`() {
        val span = rootContext().span
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
        val root = rootContext()
        val ctx = root.storeSpan(span)
        val observed = root.span.spanContext
        assertSpanContextsMatch(createInvalidSpanContext(), observed)

        val retrievedSpan = ctx.span
        assertSpanContextsMatch(spanContext.toOtelKotlinSpanContext(), retrievedSpan.spanContext)
    }

    @Test
    fun `test store span context round trip`() {
        val root = rootContext()
        val ctx = root.storeSpanContext(validSpanContext)
        assertSpanContextsMatch(validSpanContext, ctx.span.spanContext)
        assertSpanContextsMatch(createInvalidSpanContext(), root.span.spanContext)
    }

    @Test
    fun `test store invalid span context`() {
        val ctx = rootContext().storeSpanContext(createInvalidSpanContext())
        assertSpanContextsMatch(createInvalidSpanContext(), ctx.span.spanContext)
    }
}
