package io.opentelemetry.kotlin.smoketest

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.propagation.TextMapGetter
import io.opentelemetry.kotlin.propagation.TextMapSetter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Asserts that the `implementation` and `compat` backends produce byte-identical results
 * when propagating context.
 *
 * Each scenario seeds a carrier, extracts on both backends, then re-injects from the
 * resulting context. We then assert that inject and extract perform identically.
 */
@OptIn(ExperimentalApi::class)
internal class PropagationCrossCheckTest {

    @Test
    fun `w3cTraceContext extract then re-inject is identical`() {
        val seed = mapOf(
            "traceparent" to "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01",
            "tracestate" to "vendor=value",
        )
        assertRoundTripParity(seed, w3cTraceContextOtels())
    }

    @Test
    fun `w3cTraceContext extract of unsampled flags is identical`() {
        val seed = mapOf(
            "traceparent" to "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-00",
        )
        assertRoundTripParity(seed, w3cTraceContextOtels())
    }

    @Test
    fun `w3cTraceContext extract of malformed header is identical`() {
        val seed = mapOf("traceparent" to "not-a-valid-traceparent")
        val refs = w3cTraceContextOtels()
        val implOut = roundTrip(refs.impl, seed)
        val compatOut = roundTrip(refs.compat, seed)
        assertEquals(compatOut, implOut)
    }

    @Test
    fun `w3cBaggage extract then re-inject is identical`() {
        val seed = mapOf("baggage" to "userId=alice,sessionId=42")
        val refs = w3cBaggageOtels()
        val implOut = roundTrip(refs.impl, seed)
        val compatOut = roundTrip(refs.compat, seed)

        // entry order does not need to be preserved
        assertEquals(baggageEntries(compatOut), baggageEntries(implOut))
        assertEquals(setOf("userId=alice", "sessionId=42"), baggageEntries(implOut))
    }

    private fun baggageEntries(carrier: Map<String, String>): Set<String> {
        val entries = carrier["baggage"].orEmpty().split(",")
        return entries.filter(String::isNotEmpty).toSet()
    }

    private fun w3cTraceContextOtels(): OtelRefs = OtelRefs(
        impl = createOpenTelemetry { propagator { w3cTraceContext() } },
        compat = createCompatOpenTelemetry { propagator { w3cTraceContext() } }
    )

    private fun w3cBaggageOtels(): OtelRefs = OtelRefs(
        impl = createOpenTelemetry { propagator { w3cBaggage() } },
        compat = createCompatOpenTelemetry { propagator { w3cBaggage() } }
    )

    private fun assertRoundTripParity(
        seed: Map<String, String>,
        refs: OtelRefs,
    ) {
        val implOut = roundTrip(refs.impl, seed)
        val compatOut = roundTrip(refs.compat, seed)
        assertEquals(seed, implOut)
        assertEquals(seed, compatOut)
        assertEquals(compatOut, implOut)
    }

    private fun roundTrip(otel: OpenTelemetry, seed: Map<String, String>): Map<String, String> {
        val extracted = otel.propagator.extract(otel.context.root(), seed, MapTextMapGetter)
        val out = mutableMapOf<String, String>()
        otel.propagator.inject(extracted, out, MapTextMapSetter)
        return out
    }
}

private class OtelRefs(
    val impl: OpenTelemetry,
    val compat: OpenTelemetry,
)

@OptIn(ExperimentalApi::class)
private object MapTextMapGetter : TextMapGetter<Map<String, String>> {
    override fun keys(carrier: Map<String, String>): Collection<String> = carrier.keys
    override fun get(carrier: Map<String, String>?, key: String): String? = carrier?.get(key)
    override fun getAll(carrier: Map<String, String>?, key: String): List<String> =
        carrier?.get(key)?.let { listOf(it) } ?: emptyList()
}

@OptIn(ExperimentalApi::class)
private object MapTextMapSetter : TextMapSetter<MutableMap<String, String>> {
    override fun set(carrier: MutableMap<String, String>?, key: String, value: String) {
        carrier?.set(key, value)
    }
}
