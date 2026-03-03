package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import org.junit.Test
import kotlin.test.assertSame

internal class ContextFactoryImplTest {

    private val contextFactory = CompatContextFactory()
    private val spanFactory = CompatSpanFactory(CompatSpanContextFactory())

    @Test
    fun `test root`() {
        assertSame(OtelJavaContext.root(), contextFactory.root().toOtelJavaContext())
    }

    @Test
    fun `test store span`() {
        val tracer = createCompatOpenTelemetry().tracerProvider.getTracer("tracer")
        val span = tracer.startSpan("span")
        val ctx = contextFactory.storeSpan(contextFactory.root(), span)
        val retrievedSpan = spanFactory.fromContext(ctx)
        assertSpanContextsMatch(span.spanContext, retrievedSpan.spanContext)
    }

    @Test
    fun `test current`() {
        assertSame(OtelJavaContext.current(), contextFactory.implicit().toOtelJavaContext())
    }
}
