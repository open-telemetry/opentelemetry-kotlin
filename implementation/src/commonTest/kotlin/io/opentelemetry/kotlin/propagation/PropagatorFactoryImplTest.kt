package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class PropagatorFactoryImplTest {

    private val factory = PropagatorFactoryImpl()
    private val contextFactory = ContextFactoryImpl()

    @Test
    fun `composite of zero propagators returns empty propagator`() {
        val composite = factory.composite()
        assertTrue(composite.fields().isEmpty())

        val root = contextFactory.root()
        val carrier = mutableMapOf<String, String>()
        composite.inject(root, carrier, MapTextMapSetter)
        assertTrue(carrier.isEmpty())

        val ctx = composite.extract(root, carrier, MapTextMapGetter)
        assertSame(root, ctx)
    }

    @Test
    fun `composite of single propagator returns same instance`() {
        val single = RecordingPropagator(listOf("foo"))
        assertSame(single, factory.composite(single))
        assertSame(single, factory.composite(listOf(single)))
    }

    @Test
    fun `composite of many returns deduped union of fields preserving order`() {
        val a = RecordingPropagator(listOf("a", "shared"))
        val b = RecordingPropagator(listOf("b", "shared", "c"))
        val composite = factory.composite(a, b)
        assertEquals(listOf("a", "shared", "b", "c"), composite.fields().toList())
    }

    @Test
    fun `composite invokes inject on all delegates in order`() {
        val a = RecordingPropagator(listOf("a"))
        val b = RecordingPropagator(listOf("b"))
        val composite = factory.composite(a, b)
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
        val a = ContextWritingPropagator(keyA, "alpha")
        val b = ContextWritingPropagator(keyB, "beta")
        val composite = factory.composite(a, b)
        val result = composite.extract(contextFactory.root(), emptyMap(), MapTextMapGetter)
        assertEquals("alpha", result.get(keyA))
        assertEquals("beta", result.get(keyB))
    }

    @Test
    fun `composite extract observes upstream context modifications`() {
        val key: ContextKey<String> = contextFactory.createKey("k")
        val writer = ContextWritingPropagator(key, "set-by-writer")
        val reader = ContextReadingPropagator(key)
        factory.composite(writer, reader).extract(contextFactory.root(), emptyMap(), MapTextMapGetter)
        assertEquals("set-by-writer", reader.observedValue)
    }

    @Test
    fun `vararg and list overloads behave identically`() {
        val a = RecordingPropagator(listOf("a"))
        val b = RecordingPropagator(listOf("b"))
        val viaVararg = factory.composite(a, b)
        val viaList = factory.composite(listOf(a, b))
        assertEquals(viaVararg.fields().toList(), viaList.fields().toList())
    }
}

@OptIn(ExperimentalApi::class)
private class RecordingPropagator(private val keys: List<String>) : TextMapPropagator {
    var injectCalled: Boolean = false
        private set

    override fun fields(): Collection<String> = keys

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        injectCalled = true
        keys.forEach { k ->
            setter.set(carrier, k, "set-by-$k")
        }
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
    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        return context.set(key, value)
    }
}

@OptIn(ExperimentalApi::class)
private class ContextReadingPropagator(private val key: ContextKey<String>) : TextMapPropagator {
    var observedValue: String? = null
        private set

    override fun fields(): Collection<String> = emptyList()
    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {}
    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        observedValue = context.get(key)
        return context
    }
}
