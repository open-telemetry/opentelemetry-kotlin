package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.init.B3Format
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatB3PropagatorApiTest {

    @Test
    fun `b3 single returns propagator with b3 field`() {
        val dsl = CompatPropagatorConfigImpl()
        val propagator = dsl.b3(B3Format.SINGLE)
        assertEquals(listOf("b3"), propagator.fields().toList())
    }

    @Test
    fun `b3 multi returns propagator with X-B3 fields`() {
        val dsl = CompatPropagatorConfigImpl()
        val propagator = dsl.b3(B3Format.MULTI)
        assertEquals(listOf("X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled"), propagator.fields().toList())
    }

    @Test
    fun `b3 default format is SINGLE`() {
        val dsl = CompatPropagatorConfigImpl()
        val propagator = dsl.b3()
        assertEquals(listOf("b3"), propagator.fields().toList())
    }

    @Test
    fun `b3 call captures result and buildPropagator returns it`() {
        val dsl = CompatPropagatorConfigImpl()
        val captured = dsl.b3(B3Format.SINGLE)
        assertSame(captured, dsl.buildPropagator())
    }

    @Test
    fun `b3 single round-trips a sampled span`() {
        val dsl = CompatPropagatorConfigImpl()
        val contextFactory = CompatContextFactory()
        val propagator = dsl.b3(B3Format.SINGLE)
        val traceId = "0af7651916cd43dd8448eb211c80319c"
        val spanId = "b7ad6b7169203331"
        val incoming = mapOf("b3" to "$traceId-$spanId-1")
        val extracted = propagator.extract(contextFactory.root(), incoming, MapTextMapGetter)
        val outgoing = mutableMapOf<String, String>()
        propagator.inject(extracted, outgoing, MapTextMapSetter)
        assertEquals("$traceId-$spanId-1", outgoing["b3"])
    }

    @Test
    fun `b3 multi round-trips a sampled span`() {
        val dsl = CompatPropagatorConfigImpl()
        val contextFactory = CompatContextFactory()
        val propagator = dsl.b3(B3Format.MULTI)
        val traceId = "0af7651916cd43dd8448eb211c80319c"
        val spanId = "b7ad6b7169203331"
        val incoming = mapOf(
            "X-B3-TraceId" to traceId,
            "X-B3-SpanId" to spanId,
            "X-B3-Sampled" to "1",
        )
        val extracted = propagator.extract(contextFactory.root(), incoming, MapTextMapGetter)
        val outgoing = mutableMapOf<String, String>()
        propagator.inject(extracted, outgoing, MapTextMapSetter)
        assertEquals(traceId, outgoing["X-B3-TraceId"])
        assertEquals(spanId, outgoing["X-B3-SpanId"])
        assertEquals("1", outgoing["X-B3-Sampled"])
    }

    @Test
    fun `b3 returns an adapter wrapping the Java B3Propagator`() {
        val dsl = CompatPropagatorConfigImpl()
        assertTrue(dsl.b3(B3Format.SINGLE) is TextMapPropagatorAdapter)
    }
}
