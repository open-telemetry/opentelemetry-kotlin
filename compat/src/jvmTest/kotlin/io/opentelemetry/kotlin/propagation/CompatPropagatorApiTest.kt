package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.createContextKey
import io.opentelemetry.kotlin.context.rootContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatPropagatorApiTest {

    @Test
    fun `composite of zero propagators returns a working noop`() {
        val key: ContextKey<String> = createContextKey("k")
        val composite = createCompositePropagator()
        assertTrue(composite.fields().isEmpty())

        val carrier = mutableMapOf<String, String>()
        val seeded = rootContext().set(key, "value")
        composite.inject(seeded, carrier, FakeTextMapSetter)
        assertTrue(carrier.isEmpty())

        val extracted = composite.extract(seeded, carrier, FakeTextMapGetter)
        assertEquals("value", extracted.get(key))
    }

    @Test
    fun `composite of single Kotlin propagator round-trips through Java`() {
        val recording = RecordingPropagator(listOf("foo"))
        val composite = createCompositePropagator(recording)

        val carrier = mutableMapOf<String, String>()
        composite.inject(rootContext(), carrier, FakeTextMapSetter)
        assertTrue(recording.injectCalled)
        assertEquals(mapOf("foo" to "set-by-foo"), carrier)

        composite.extract(rootContext(), carrier, FakeTextMapGetter)
        assertTrue(recording.extractCalled)
    }

    @Test
    fun `composite invokes inject on all delegates in order`() {
        val a = RecordingPropagator(listOf("a"))
        val b = RecordingPropagator(listOf("b"))
        val composite = createCompositePropagator(a, b)

        val carrier = linkedMapOf<String, String>()
        composite.inject(rootContext(), carrier, FakeTextMapSetter)

        assertEquals(listOf("a", "b"), carrier.keys.toList())
        assertTrue(a.injectCalled)
        assertTrue(b.injectCalled)
    }

    @Test
    fun `composite threads context through extract delegates left-to-right`() {
        val keyA: ContextKey<String> = createContextKey("a")
        val keyB: ContextKey<String> = createContextKey("b")
        val composite = createCompositePropagator(
            ContextWritingPropagator(keyA, "alpha"),
            ContextWritingPropagator(keyB, "beta"),
        )

        val result = composite.extract(rootContext(), emptyMap(), FakeTextMapGetter)

        assertEquals("alpha", result.get(keyA))
        assertEquals("beta", result.get(keyB))
    }

    @Test
    fun `w3cBaggage declares the baggage field`() {
        val propagator = createW3CBaggagePropagator()
        assertEquals(listOf("baggage"), propagator.fields().toList())
    }

    @Test
    fun `w3cTraceContext declares the traceparent and tracestate fields`() {
        val propagator = createW3CTraceContextPropagator()
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())
    }

    @Test
    fun `w3cTraceContext round-trips a traceparent header through inject and extract`() {
        val propagator = createW3CTraceContextPropagator()
        val incoming = mapOf(
            "traceparent" to "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01",
            "tracestate" to "vendor=value",
        )
        val extracted = propagator.extract(rootContext(), incoming, FakeTextMapGetter)

        val outgoing = mutableMapOf<String, String>()
        propagator.inject(extracted, outgoing, FakeTextMapSetter)

        assertEquals(incoming["traceparent"], outgoing["traceparent"])
        assertEquals(incoming["tracestate"], outgoing["tracestate"])
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
