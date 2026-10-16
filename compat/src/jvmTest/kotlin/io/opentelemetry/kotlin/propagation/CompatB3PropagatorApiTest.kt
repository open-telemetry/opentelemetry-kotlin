package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.dsl.PropagatorConfigImpl
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.init.B3Format
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CompatB3PropagatorApiTest {

    @Test
    fun `b3 single returns propagator with b3 field`() {
        val dsl = PropagatorConfigImpl()
        val propagator = dsl.b3(B3Format.SINGLE)
        assertEquals(listOf("b3"), propagator.fields().toList())
    }

    @Test
    fun `b3 multi returns propagator with X-B3 fields`() {
        val dsl = PropagatorConfigImpl()
        val propagator = dsl.b3(B3Format.MULTI)
        assertEquals(listOf("X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled"), propagator.fields().toList())
    }

    @Test
    fun `b3 default format is SINGLE`() {
        val dsl = PropagatorConfigImpl()
        val propagator = dsl.b3()
        assertEquals(listOf("b3"), propagator.fields().toList())
    }

    @Test
    fun `b3 single round-trips a sampled span`() {
        val dsl = PropagatorConfigImpl()
        val contextFactory = CompatContextFactory()
        val propagator = dsl.b3(B3Format.SINGLE)
        val traceId = "0af7651916cd43dd8448eb211c80319c"
        val spanId = "b7ad6b7169203331"
        val incoming = mapOf("b3" to "$traceId-$spanId-1")
        val extracted = propagator.extract(contextFactory.root(), incoming, FakeTextMapGetter)
        val outgoing = mutableMapOf<String, String>()
        propagator.inject(extracted, outgoing, FakeTextMapSetter)
        assertEquals("$traceId-$spanId-1", outgoing["b3"])
    }

    @Test
    fun `b3 multi round-trips a sampled span`() {
        val dsl = PropagatorConfigImpl()
        val contextFactory = CompatContextFactory()
        val propagator = dsl.b3(B3Format.MULTI)
        val traceId = "0af7651916cd43dd8448eb211c80319c"
        val spanId = "b7ad6b7169203331"
        val incoming = mapOf(
            "X-B3-TraceId" to traceId,
            "X-B3-SpanId" to spanId,
            "X-B3-Sampled" to "1",
        )
        val extracted = propagator.extract(contextFactory.root(), incoming, FakeTextMapGetter)
        val outgoing = mutableMapOf<String, String>()
        propagator.inject(extracted, outgoing, FakeTextMapSetter)
        assertEquals(traceId, outgoing["X-B3-TraceId"])
        assertEquals(spanId, outgoing["X-B3-SpanId"])
        assertEquals("1", outgoing["X-B3-Sampled"])
    }

    @Test
    fun `b3 returns the api B3 propagator`() {
        val dsl = PropagatorConfigImpl()
        assertSame(createB3Propagator(B3Format.SINGLE), dsl.b3(B3Format.SINGLE))
        assertSame(createB3Propagator(B3Format.MULTI), dsl.b3(B3Format.MULTI))
    }

    @Test
    fun `b3 debug flag round-trips through a compat context`() {
        val propagator = PropagatorConfigImpl().b3(B3Format.SINGLE)
        val incoming = mapOf("b3" to "0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-d")
        val extracted = propagator.extract(CompatContextFactory().root(), incoming, FakeTextMapGetter)
        val outgoing = mutableMapOf<String, String>()
        propagator.inject(extracted, outgoing, FakeTextMapSetter)
        assertEquals(incoming["b3"], outgoing["b3"])
    }
}
