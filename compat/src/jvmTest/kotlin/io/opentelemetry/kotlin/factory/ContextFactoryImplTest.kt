package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.assertions.assertSpanContextsMatch
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import org.junit.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class ContextFactoryImplTest {

    private val factory = createCompatSdkFactory()

    @Test
    fun `test root`() {
        assertSame(OtelJavaContext.root(), factory.contextFactory.root().toOtelJavaContext())
    }

    @Test
    fun `test store span`() {
        val tracer = createCompatOpenTelemetry().tracerProvider.getTracer("tracer")
        val span = tracer.startSpan("span")
        val contextFactory = factory.contextFactory
        val ctx = contextFactory.storeSpan(contextFactory.root(), span)
        val retrievedSpan = factory.spanFactory.fromContext(ctx)
        assertSpanContextsMatch(span.spanContext, retrievedSpan.spanContext)
    }

    @Test
    fun `test current`() {
        assertSame(OtelJavaContext.current(), factory.contextFactory.implicitContext().toOtelJavaContext())
    }
}
