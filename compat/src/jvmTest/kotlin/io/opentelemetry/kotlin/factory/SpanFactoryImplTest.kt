package io.opentelemetry.kotlin.factory

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SpanFactoryImplTest {

    private val spanContextFactory = CompatSpanContextFactory()
    private val traceStateFactory = CompatTraceStateFactory()
    private val traceFlagsFactory = CompatTraceFlagsFactory()
    private val contextFactory = CompatContextFactory()
    private val spanFactory = CompatSpanFactory(spanContextFactory)

    @Test
    fun `test invalid is the same instance`() {
        assertSame(spanContextFactory.invalid, spanContextFactory.invalid)
    }

    @Test
    fun `test from context`() {
        val ctx = contextFactory.root()
        val span = spanFactory.fromContext(ctx)
        assertFalse(span.spanContext.isValid)
    }

    @Test
    fun `test from span context`() {
        val generator = CompatIdGenerator()
        val spanContext = spanContextFactory.create(
            traceIdBytes = generator.generateTraceIdBytes(),
            spanIdBytes = generator.generateSpanIdBytes(),
            traceState = traceStateFactory.default,
            traceFlags = traceFlagsFactory.default,
        )
        val span = spanFactory.fromSpanContext(spanContext)
        assertTrue(span.spanContext.isValid)
    }
}
