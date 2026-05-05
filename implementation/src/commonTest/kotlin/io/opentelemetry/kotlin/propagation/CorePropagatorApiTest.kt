package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.PropagatorConfigImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CorePropagatorApiTest {

    private val dsl = PropagatorConfigImpl()
    private val contextFactory = ContextFactoryImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(IdGeneratorImpl(), traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory, contextFactory.spanKey)

    @Test
    fun `composite with single propagator wraps in CompositeTextMapPropagator`() {
        val single = RecordingPropagator(listOf("foo"))
        val composite = dsl.composite(single)
        assertIs<CompositeTextMapPropagator>(composite)
        assertEquals(listOf("foo"), composite.fields().toList())
    }

    @Test
    fun `composite returns deduped union of fields preserving order`() {
        val a = RecordingPropagator(listOf("a", "shared"))
        val b = RecordingPropagator(listOf("b", "shared", "c"))
        val composite = dsl.composite(a, b)
        assertEquals(listOf("a", "shared", "b", "c"), composite.fields().toList())
    }

    @Test
    fun `composite invokes inject on all delegates in order`() {
        val a = RecordingPropagator(listOf("a"))
        val b = RecordingPropagator(listOf("b"))
        val composite = dsl.composite(a, b)
        val carrier = mutableMapOf<String, String>()
        composite.inject(contextFactory.root(), carrier, MapTextMapSetter)
        assertEquals(listOf("a", "b"), carrier.keys.toList())
        assertTrue(a.injectCalled)
        assertTrue(b.injectCalled)
    }

    @Test
    fun `composite threads context through extract delegates left-to-right`() {
        val keyA: ContextKey<String> = contextFactory.createKey("a")
        val keyB: ContextKey<String> = contextFactory.createKey("b")
        val composite = dsl.composite(
            ContextWritingPropagator(keyA, "alpha"),
            ContextWritingPropagator(keyB, "beta"),
        )
        val result = composite.extract(contextFactory.root(), emptyMap(), MapTextMapGetter)
        assertEquals("alpha", result.get(keyA))
        assertEquals("beta", result.get(keyB))
    }

    @Test
    fun `w3cBaggage returns the W3C baggage propagator singleton`() {
        assertSame(W3CBaggagePropagator, dsl.w3cBaggage())
    }

    @Test
    fun `w3cBaggage call captures the result and buildPropagator returns it`() {
        dsl.w3cBaggage()
        assertSame(W3CBaggagePropagator, dsl.buildPropagator())
    }

    @Test
    fun `composite call captures the result and buildPropagator returns it`() {
        val captured = dsl.composite(RecordingPropagator(listOf("foo")))
        assertSame(captured, dsl.buildPropagator())
    }

    @Test
    fun `w3cTraceContext returns a propagator for traceparent and tracestate fields`() {
        val propagator = dsl.w3cTraceContext()
        installFactories()
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())
    }

    @Test
    fun `w3cTraceContext call captures the result and buildPropagator returns it`() {
        val captured = dsl.w3cTraceContext()
        assertSame(captured, dsl.buildPropagator())
    }

    private fun installFactories() {
        dsl.installFactories(
            traceFlagsFactory = traceFlagsFactory,
            traceStateFactory = traceStateFactory,
            spanContextFactory = spanContextFactory,
            spanFactory = spanFactory,
            contextFactory = contextFactory,
        )
    }
}

@OptIn(ExperimentalApi::class)
private class RecordingPropagator(private val keys: List<String>) : TextMapPropagator {
    var injectCalled: Boolean = false
        private set

    override fun fields(): Collection<String> = keys

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        injectCalled = true
        keys.forEach { k -> setter.set(carrier, k, "set-by-$k") }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = context
}

@OptIn(ExperimentalApi::class)
private class ContextWritingPropagator(
    private val key: ContextKey<String>,
    private val value: String,
) : TextMapPropagator {
    override fun fields(): Collection<String> = emptyList()
    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {}
    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context =
        context.set(key, value)
}
