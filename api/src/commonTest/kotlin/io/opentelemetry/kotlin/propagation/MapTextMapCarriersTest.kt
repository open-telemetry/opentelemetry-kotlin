package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.tracing.createSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class MapTextMapCarriersTest {

    @Test
    fun getterSupportsHttpHeaderCasingAndAllKeys() {
        val getter = createMapTextMapGetter()
        val carrier = linkedMapOf("TraceParent" to "first", "traceparent" to "second", "Baggage" to "")

        assertEquals(carrier.keys, getter.keys(carrier))
        assertEquals("first", getter.get(carrier, "TRACEPARENT"))
        assertEquals(listOf("first", "second"), getter.getAll(carrier, "traceparent"))
        assertEquals(listOf(""), getter.getAll(carrier, "baggage"))
        assertNull(getter.get(carrier, "missing"))
        assertEquals(emptyList(), getter.getAll(carrier, "missing"))
    }

    @Test
    fun setterSupportsNarrowerCarrierTypesAndReplacesValues() {
        val setter: TextMapSetter<HashMap<String, String>> = createMutableMapTextMapSetter()
        val carrier = hashMapOf("traceparent" to "old", "other" to "preserved")

        setter.set(carrier, "traceparent", "new")
        setter.set(carrier, "Baggage", "user=alice")

        assertEquals(mapOf("traceparent" to "new", "other" to "preserved", "Baggage" to "user=alice"), carrier)
    }

    @Test
    fun carriersAreSharedAndDoNotRetainCarrierState() {
        assertSame(createMapTextMapGetter(), createMapTextMapGetter())
        assertSame(createMutableMapTextMapSetter(), createMutableMapTextMapSetter())
        val getter = createMapTextMapGetter()
        assertEquals("first", getter.get(mapOf("key" to "first"), "key"))
        assertEquals("second", getter.get(mapOf("key" to "second"), "key"))
        assertNull(getter.get(emptyMap(), "key"))
    }

    @Test
    fun traceContextRoundTripsThroughMapCarriers() {
        val spanContext = createSpanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331")
        val context = rootContext().storeSpanContext(spanContext)
        val carrier = mutableMapOf<String, String>()
        val propagator = createW3CTraceContextPropagator()

        propagator.inject(context, carrier, createMutableMapTextMapSetter())
        val headers = carrier.mapKeys { (key, _) -> key.uppercase() }
        val extracted = propagator.extract(rootContext(), headers, createMapTextMapGetter())

        val extractedSpanContext = extracted.extractSpan().spanContext
        assertEquals(spanContext.traceId, extractedSpanContext.traceId)
        assertEquals(spanContext.spanId, extractedSpanContext.spanId)
        assertEquals(spanContext.traceFlags, extractedSpanContext.traceFlags)
        assertTrue(extractedSpanContext.isRemote)
    }
}
