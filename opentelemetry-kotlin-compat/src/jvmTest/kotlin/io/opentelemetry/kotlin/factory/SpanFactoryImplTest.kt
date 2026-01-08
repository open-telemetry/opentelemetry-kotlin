package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanFactoryImplTest {

    private val factory = createCompatSdkFactory()

    @Test
    fun `test invalid`() {
        assertSame(factory.spanContextFactory.invalid, factory.spanContextFactory.invalid)
    }

    @Test
    fun `test from context`() {
        val ctx = factory.contextFactory.root()
        val span = factory.spanFactory.fromContext(ctx)
        assertFalse(span.spanContext.isValid)
    }

    @Test
    fun `test from span context`() {
        val generator = CompatTracingIdFactory()
        val spanContext = factory.spanContextFactory.create(
            traceIdBytes = generator.generateTraceIdBytes(),
            spanIdBytes = generator.generateSpanIdBytes(),
            traceState = factory.traceStateFactory.default,
            traceFlags = factory.traceFlagsFactory.default,
        )
        val span = factory.spanFactory.fromSpanContext(spanContext)
        assertTrue(span.spanContext.isValid)
    }
}
