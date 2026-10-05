package io.opentelemetry.kotlin.smoketest

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapGetter
import io.opentelemetry.kotlin.aliases.OtelJavaTextMapSetter
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.toOtelJavaApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class JavaApiPropagationSmokeTest {

    private val traceId = "0af7651916cd43dd8448eb211c80319c"
    private val seed = mapOf(
        "traceparent" to "00-$traceId-b7ad6b7169203331-01",
        "tracestate" to "vendor=value",
    )

    @Test
    fun `impl java api round trips w3cTraceContext`() {
        assertRoundTrip(createOpenTelemetry { propagator { w3cTraceContext() } })
    }

    @Test
    fun `compat java api round trips w3cTraceContext`() {
        assertRoundTrip(createCompatOpenTelemetry { propagator { w3cTraceContext() } })
    }

    @Test
    fun `impl java api propagates remote parent to child span`() {
        assertChildSpanPropagated(createOpenTelemetry { propagator { w3cTraceContext() } })
    }

    @Test
    fun `compat java api propagates remote parent to child span`() {
        assertChildSpanPropagated(createCompatOpenTelemetry { propagator { w3cTraceContext() } })
    }

    @Test
    fun `impl java api round trips w3cBaggage`() {
        assertBaggageRoundTrip(createOpenTelemetry { propagator { w3cBaggage() } })
    }

    @Test
    fun `compat java api round trips w3cBaggage`() {
        assertBaggageRoundTrip(createCompatOpenTelemetry { propagator { w3cBaggage() } })
    }

    private fun assertRoundTrip(otel: OpenTelemetry) {
        val propagator = otel.toOtelJavaApi().propagators.textMapPropagator
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())

        val extracted = propagator.extract(OtelJavaContext.root(), seed, MapGetter)
        val out = mutableMapOf<String, String>()
        propagator.inject(extracted, out, MapSetter)
        assertEquals(seed, out)
    }

    private fun assertChildSpanPropagated(otel: OpenTelemetry) {
        val javaApi = otel.toOtelJavaApi()
        val propagator = javaApi.propagators.textMapPropagator
        val extracted = propagator.extract(OtelJavaContext.root(), seed, MapGetter)

        val span = javaApi.tracerProvider.get("tracer")
            .spanBuilder("child")
            .setParent(extracted)
            .startSpan()
        val out = mutableMapOf<String, String>()
        propagator.inject(OtelJavaSpan.wrap(span.spanContext).storeInContext(OtelJavaContext.root()), out, MapSetter)
        span.end()

        val traceparent = out.getValue("traceparent")
        assertTrue(traceparent.startsWith("00-$traceId-"), traceparent)
        assertNotEquals(seed["traceparent"], traceparent)
    }

    private fun assertBaggageRoundTrip(otel: OpenTelemetry) {
        val propagator = otel.toOtelJavaApi().propagators.textMapPropagator
        val extracted = propagator.extract(
            OtelJavaContext.root(),
            mapOf("baggage" to "userId=alice"),
            MapGetter,
        )
        val out = mutableMapOf<String, String>()
        propagator.inject(extracted, out, MapSetter)
        assertEquals(mapOf("baggage" to "userId=alice"), out)
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
