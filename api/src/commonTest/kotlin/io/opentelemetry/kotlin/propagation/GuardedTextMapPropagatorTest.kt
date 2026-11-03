package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.tracing.createSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class GuardedTextMapPropagatorTest {

    private val builtIns = listOf(
        createW3CTraceContextPropagator(),
        createW3CBaggagePropagator(),
        createB3Propagator(B3Format.SINGLE),
        createB3Propagator(B3Format.MULTI),
        createCompositePropagator(createW3CTraceContextPropagator(), createW3CBaggagePropagator()),
    )

    private val populatedContext: Context = rootContext()
        .storeSpanContext(createSpanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331"))
        .storeBaggage(createBaggage().set("user", "alice"))

    @Test
    fun `throwing setter does not escape built-in propagators`() {
        builtIns.forEach { propagator ->
            propagator.inject(populatedContext, mutableMapOf(), ThrowingTextMapSetter)
        }
    }

    @Test
    fun `throwing getter returns the supplied context from built-in propagators`() {
        val ctx = rootContext()
        builtIns.forEach { propagator ->
            assertSame(ctx, propagator.extract(ctx, emptyMap(), ThrowingTextMapGetter))
        }
    }

    @Test
    fun `composite skips a throwing delegate and runs the rest`() {
        val composite = createCompositePropagator(
            createW3CTraceContextPropagator(),
            ThrowingPropagator,
            createW3CBaggagePropagator(),
        )
        assertEquals(listOf("traceparent", "tracestate", "baggage"), composite.fields().toList())

        val carrier = mutableMapOf<String, String>()
        composite.inject(populatedContext, carrier, FakeTextMapSetter)
        assertEquals(setOf("traceparent", "baggage"), carrier.keys)

        val extracted = composite.extract(rootContext(), carrier, FakeTextMapGetter)
        assertEquals("b7ad6b7169203331", extracted.extractSpan().spanContext.spanId)
        assertEquals("alice", extracted.extractBaggage().getValue("user"))
    }

    private object ThrowingPropagator : TextMapPropagator {
        override fun fields(): Collection<String> = error("boom")
        override fun <T> inject(context: Context, carrier: T, setter: TextMapSetter<T>): Unit = error("boom")
        override fun <T> extract(context: Context, carrier: T, getter: TextMapGetter<T>): Context = error("boom")
    }

    private object ThrowingTextMapSetter : TextMapSetter<MutableMap<String, String>> {
        override fun set(carrier: MutableMap<String, String>, key: String, value: String): Unit = error("boom")
    }

    private object ThrowingTextMapGetter : TextMapGetter<Map<String, String>> {
        override fun keys(carrier: Map<String, String>): Collection<String> = error("boom")
        override fun get(carrier: Map<String, String>, key: String): String? = error("boom")
        override fun getAll(carrier: Map<String, String>, key: String): List<String> = error("boom")
    }
}
