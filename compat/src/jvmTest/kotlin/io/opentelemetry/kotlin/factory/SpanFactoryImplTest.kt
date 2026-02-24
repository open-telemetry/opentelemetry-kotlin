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
        assertSame(factory.spanContext.invalid, factory.spanContext.invalid)
    }

    @Test
    fun `test from context`() {
        val ctx = factory.context.root()
        val span = factory.span.fromContext(ctx)
        assertFalse(span.spanContext.isValid)
    }

    @Test
    fun `test from span context`() {
        val generator = CompatTracingIdFactory()
        val spanContext = factory.spanContext.create(
            traceIdBytes = generator.generateTraceIdBytes(),
            spanIdBytes = generator.generateSpanIdBytes(),
            traceState = factory.traceState.default,
            traceFlags = factory.traceFlags.default,
        )
        val span = factory.span.fromSpanContext(spanContext)
        assertTrue(span.spanContext.isValid)
    }
}
