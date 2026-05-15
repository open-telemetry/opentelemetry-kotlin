package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.propagation.MapTextMapGetter
import io.opentelemetry.kotlin.propagation.MapTextMapSetter
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class PropagatorConfigImplTest {

    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val idGenerator = IdGeneratorImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)
    private val contextWithSpan = contextFactory.root().storeSpan(spanFactory.fromSpanContext(FakeSpanContext.VALID))

    @Test
    fun `w3cTraceContext does nothing if factories are not installed`() {
        val propagator = PropagatorConfigImpl().apply { w3cTraceContext() }.buildPropagator()
        assertEquals(emptyList(), propagator.fields().toList())

        val carrier = mutableMapOf<String, String>()
        propagator.inject(contextWithSpan, carrier, MapTextMapSetter)
        assertTrue(carrier.isEmpty())

        val result = propagator.extract(
            context = contextFactory.root(),
            carrier = mapOf("traceparent" to "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"),
            getter = MapTextMapGetter
        )
        assertSame(contextFactory.root(), result)
    }

    @Test
    fun `w3cTraceContext routes through delegate once factories are installed`() {
        val config = PropagatorConfigImpl().apply { w3cTraceContext() }
        config.installFactories(traceFlagsFactory, traceStateFactory, spanContextFactory, spanFactory)
        val propagator = config.buildPropagator()
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())

        val carrier = mutableMapOf<String, String>()
        propagator.inject(contextWithSpan, carrier, MapTextMapSetter)
        assertTrue(carrier.contains("traceparent"))

        val result = propagator.extract(
            context = contextFactory.root(),
            carrier = mapOf("traceparent" to "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"),
            getter = MapTextMapGetter
        )
        assertNotSame(contextFactory.root(), result)
    }
}
