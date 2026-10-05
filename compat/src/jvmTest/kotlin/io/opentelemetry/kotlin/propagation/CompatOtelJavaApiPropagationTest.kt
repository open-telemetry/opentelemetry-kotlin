package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatOtelJavaApiPropagationTest {

    private val traceId = "0af7651916cd43dd8448eb211c80319c"
    private val seed = mapOf(
        "traceparent" to "00-$traceId-b7ad6b7169203331-01",
        "tracestate" to "vendor=value",
    )

    @Test
    fun `java api round trips w3cTraceContext`() {
        val otel = createCompatOpenTelemetry { propagator { w3cTraceContext() } }
        val propagator = otel.toOtelJavaApi().propagators.textMapPropagator
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())

        val extracted = propagator.extract(OtelJavaContext.root(), seed, MapGetter)
        val out = mutableMapOf<String, String>()
        propagator.inject(extracted, out, MapSetter)
        assertEquals(seed, out)
    }

    @Test
    fun `java api propagates remote parent to child span`() {
        val otel = createCompatOpenTelemetry { propagator { w3cTraceContext() } }
        val javaApi = otel.toOtelJavaApi()
        val propagator = javaApi.propagators.textMapPropagator
        val extracted = propagator.extract(OtelJavaContext.root(), seed, MapGetter)
        val span = javaApi.tracerProvider.get("tracer")
            .spanBuilder("child")
            .setParent(extracted)
            .startSpan()
        val out = mutableMapOf<String, String>()
        val ctx = OtelJavaSpan.wrap(span.spanContext).storeInContext(OtelJavaContext.root())
        propagator.inject(ctx, out, MapSetter)
        span.end()

        val traceparent = out.getValue("traceparent")
        assertTrue(traceparent.startsWith("00-$traceId-"), traceparent)
        assertNotEquals(seed["traceparent"], traceparent)
    }

    @Test
    fun `java api round trips w3cBaggage`() {
        val otel = createCompatOpenTelemetry { propagator { w3cBaggage() } }
        val propagator = otel.toOtelJavaApi().propagators.textMapPropagator
        val carrier = mapOf("baggage" to "userId=alice")

        val extracted = propagator.extract(OtelJavaContext.root(), carrier, MapGetter)
        val out = mutableMapOf<String, String>()
        propagator.inject(extracted, out, MapSetter)
        assertEquals(carrier, out)
    }

    private object MapGetter : OtelJavaTextMapGetter<Map<String, String>> {
        override fun keys(carrier: Map<String, String>): Iterable<String> = carrier.keys
        override fun get(carrier: Map<String, String>?, key: String): String? = carrier?.get(key)
    }

    private object MapSetter : OtelJavaTextMapSetter<MutableMap<String, String>> {
        override fun set(carrier: MutableMap<String, String>?, key: String, value: String) {
            carrier?.put(key, value)
        }
    }
}
