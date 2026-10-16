package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.FakeTextMapSetter
import io.opentelemetry.kotlin.propagation.createB3Propagator
import io.opentelemetry.kotlin.propagation.createW3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.createW3CTraceContextPropagator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class PropagatorConfigImplTest {

    private val dsl = PropagatorConfigImpl()

    @Test
    fun `composite with single propagator delegates to it`() {
        val single = FakeTextMapPropagator(listOf("foo"))
        val composite = dsl.composite(single)
        assertEquals(listOf("foo"), composite.fields().toList())

        val carrier = mutableMapOf<String, String>()
        composite.inject(FakeContext(), carrier, FakeTextMapSetter)
        assertTrue(single.injectCalled)
        assertEquals(listOf("foo"), carrier.keys.toList())
    }

    @Test
    fun `w3cBaggage returns the W3C baggage propagator singleton`() {
        assertSame(createW3CBaggagePropagator(), dsl.w3cBaggage())
    }

    @Test
    fun `w3cTraceContext returns the W3C trace context propagator singleton`() {
        val propagator = dsl.w3cTraceContext()
        assertSame(createW3CTraceContextPropagator(), propagator)
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())
    }

    @Test
    fun `b3 single returns propagator with b3 field`() {
        val propagator = dsl.b3(B3Format.SINGLE)
        assertSame(createB3Propagator(B3Format.SINGLE), propagator)
        assertEquals(listOf("b3"), propagator.fields().toList())
    }

    @Test
    fun `b3 multi returns propagator with X-B3 fields`() {
        val propagator = dsl.b3(B3Format.MULTI)
        assertSame(createB3Propagator(B3Format.MULTI), propagator)
        assertEquals(listOf("X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled"), propagator.fields().toList())
    }

    @Test
    fun `b3 default format is SINGLE`() {
        val propagator = dsl.b3()
        assertEquals(listOf("b3"), propagator.fields().toList())
    }
}
