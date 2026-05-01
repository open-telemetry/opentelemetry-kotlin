package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapPropagator
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.factory.CompatContextFactory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatPropagatorApiTest {

    private val dsl = CompatPropagatorConfigImpl()
    private val contextFactory = CompatContextFactory()

    @Test
    fun `composite of zero propagators returns a working noop`() {
        val key: ContextKey<String> = contextFactory.createKey("k")
        val composite = dsl.composite()
        assertTrue(composite.fields().isEmpty())

        val carrier = mutableMapOf<String, String>()
        val seeded = contextFactory.root().set(key, "value")
        composite.inject(seeded, carrier, MapTextMapSetter)
        assertTrue(carrier.isEmpty())

        val extracted = composite.extract(seeded, carrier, MapTextMapGetter)
        assertEquals("value", extracted.get(key))
    }

    @Test
    fun `composite of single Kotlin propagator round-trips through Java`() {
        val recording = RecordingPropagator(listOf("foo"))
        val composite = dsl.composite(recording)

        val carrier = mutableMapOf<String, String>()
        composite.inject(contextFactory.root(), carrier, MapTextMapSetter)
        assertTrue(recording.injectCalled)
        assertEquals(mapOf("foo" to "set-by-foo"), carrier)

        composite.extract(contextFactory.root(), carrier, MapTextMapGetter)
        assertTrue(recording.extractCalled)
    }

    @Test
    fun `composite invokes inject on all delegates in order`() {
        val a = RecordingPropagator(listOf("a"))
        val b = RecordingPropagator(listOf("b"))
        val composite = dsl.composite(a, b)

        val carrier = linkedMapOf<String, String>()
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
    fun `composite unwraps adapters before delegating to Java`() {
        val javaPropagator: OtelJavaTextMapPropagator = OtelJavaTextMapPropagator.noop()
        val adapter = TextMapPropagatorAdapter(javaPropagator)

        val composite = dsl.composite(adapter)

        assertTrue(composite is TextMapPropagatorAdapter)
        assertSame(javaPropagator, composite.impl)
    }

    @Test
    fun `w3cBaggage returns an adapter wrapping the Java W3CBaggagePropagator`() {
        val propagator = dsl.w3cBaggage()
        assertTrue(propagator is TextMapPropagatorAdapter)
        assertEquals(listOf("baggage"), propagator.fields().toList())
    }

    @Test
    fun `composite call captures the result and buildPropagator returns it`() {
        val captured = dsl.composite(RecordingPropagator(listOf("foo")))
        assertSame(captured, dsl.buildPropagator())
    }

    @Test
    fun `w3cBaggage call captures the result and buildPropagator returns it`() {
        val captured = dsl.w3cBaggage()
        assertSame(captured, dsl.buildPropagator())
    }
}

@OptIn(ExperimentalApi::class)
private class RecordingPropagator(private val keys: List<String>) : TextMapPropagator {
    var injectCalled: Boolean = false
        private set
    var extractCalled: Boolean = false
        private set

    override fun fields(): Collection<String> = keys

    override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>) {
        injectCalled = true
        keys.forEach { setter.set(carrier, it, "set-by-$it") }
    }

    override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context {
        extractCalled = true
        return context
    }
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

@OptIn(ExperimentalApi::class)
private object MapTextMapGetter : TextMapGetter<Map<String, String>> {
    override fun keys(carrier: Map<String, String>): Collection<String> = carrier.keys
    override fun get(carrier: Map<String, String>, key: String): String? = carrier[key]
}

@OptIn(ExperimentalApi::class)
private object MapTextMapSetter : TextMapSetter<MutableMap<String, String>> {
    override fun set(carrier: MutableMap<String, String>, key: String, value: String) {
        carrier[key] = value
    }
}
