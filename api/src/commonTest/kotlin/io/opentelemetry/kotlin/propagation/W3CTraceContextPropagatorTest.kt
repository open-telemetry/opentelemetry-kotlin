package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.Scope
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class W3CTraceContextPropagatorTest {

    private val propagator = createW3CTraceContextPropagator()

    @Test
    fun `extract combines multiple tracestate headers in order`() {
        val carrier = mapOf(
            "traceparent" to listOf(TRACEPARENT),
            "tracestate" to listOf("vendor1=opaque1", "vendor2=opaque2,vendor3=opaque3"),
        )

        val spanContext = extract(carrier)

        assertEquals(
            listOf("vendor1", "vendor2", "vendor3"),
            spanContext.traceState.asMap().keys.toList(),
        )
    }

    @Test
    fun `extract applies the 32 entry limit across tracestate headers`() {
        val carrier = mapOf(
            "traceparent" to listOf(TRACEPARENT),
            "tracestate" to listOf(
                (0 until 20).joinToString(",") { "k$it=v" },
                (20 until 40).joinToString(",") { "k$it=v" },
            ),
        )

        val traceState = extract(carrier).traceState

        assertEquals(32, traceState.asMap().size)
        assertEquals("v", traceState.get("k31"))
        assertNull(traceState.get("k32"))
    }

    private fun extract(carrier: Map<String, List<String>>): SpanContext {
        val result = propagator.extract(CapturingContext(), carrier, MultiValueMapTextMapGetter)
        return assertNotNull((result as CapturingContext).spanContext)
    }

    private class CapturingContext(
        val spanContext: SpanContext? = null,
    ) : Context {
        override fun <T> set(key: ContextKey<T>, value: T?): Context = error("Not supported")
        override fun <T> get(key: ContextKey<T>): T? = error("Not supported")
        override fun attach(): Scope = error("Not supported")
        override fun storeSpan(span: Span): Context = error("Not supported")
        override fun storeSpanContext(spanContext: SpanContext): Context = CapturingContext(spanContext)
        override fun extractSpan(): Span = error("Not supported")
        override fun storeBaggage(baggage: Baggage): Context = error("Not supported")
        override fun extractBaggage(): Baggage = error("Not supported")
        override fun clearBaggage(): Context = error("Not supported")
    }

    private object MultiValueMapTextMapGetter : TextMapGetter<Map<String, List<String>>> {
        override fun keys(carrier: Map<String, List<String>>): Collection<String> = carrier.keys
        override fun get(carrier: Map<String, List<String>>?, key: String): String? =
            carrier?.get(key)?.firstOrNull()
        override fun getAll(carrier: Map<String, List<String>>?, key: String): List<String> =
            carrier?.get(key).orEmpty()
    }

    private companion object {
        const val TRACEPARENT = "00-00000000000000000000000000000001-0000000000000001-01"
    }
}
