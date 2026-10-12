package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.tracing.compat.createSpanContext
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SpanFactoryImplTest {

    private val contextFactory = CompatContextFactory()
    private val spanFactory = CompatSpanFactory()

    @Test
    fun `test invalid is the same instance`() {
        assertSame(spanFactory.invalid, spanFactory.invalid)
    }

    @Test
    fun `test from context`() {
        val ctx = contextFactory.root()
        val span = ctx.extractSpan()
        assertFalse(span.spanContext.isValid)
    }

    @Test
    fun `test from span context`() {
        val generator = CompatIdGenerator()
        val spanContext = createSpanContext(generator.generateTraceIdBytes(), generator.generateSpanIdBytes())
        val span = spanFactory.fromSpanContext(spanContext)
        assertTrue(span.spanContext.isValid)
    }
}
