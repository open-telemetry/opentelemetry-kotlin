package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.factory.DefaultTraceFlagsFactory
import io.opentelemetry.kotlin.factory.DefaultTraceStateFactory
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.init.B3Format
import io.opentelemetry.kotlin.init.PropagatorConfigImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CorePropagatorApiTest {

    private val dsl = PropagatorConfigImpl()
    private val traceFlagsFactory = DefaultTraceFlagsFactory
    private val traceStateFactory = DefaultTraceStateFactory
    private val spanContextFactory = DefaultSpanContextFactory
    private val spanFactory = SpanFactoryImpl(spanContextFactory)

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
    fun `w3cBaggage call captures the result and buildPropagator returns it`() {
        dsl.w3cBaggage()
        assertSame(createW3CBaggagePropagator(), dsl.buildPropagator())
    }

    @Test
    fun `composite call captures the result and buildPropagator returns it`() {
        val captured = dsl.composite(FakeTextMapPropagator(listOf("foo")))
        assertSame(captured, dsl.buildPropagator())
    }

    @Test
    fun `w3cTraceContext returns a propagator for traceparent and tracestate fields`() {
        val propagator = dsl.w3cTraceContext()
        installFactories()
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())
    }

    @Test
    fun `w3cTraceContext call captures the result and buildPropagator returns it`() {
        val captured = dsl.w3cTraceContext()
        assertSame(captured, dsl.buildPropagator())
    }

    @Test
    fun `b3 single returns propagator with b3 field`() {
        val propagator = dsl.b3(B3Format.SINGLE)
        installFactories()
        assertEquals(listOf("b3"), propagator.fields().toList())
    }

    @Test
    fun `b3 multi returns propagator with X-B3 fields`() {
        val propagator = dsl.b3(B3Format.MULTI)
        installFactories()
        assertEquals(listOf("X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled"), propagator.fields().toList())
    }

    @Test
    fun `b3 default format is SINGLE`() {
        val propagator = dsl.b3()
        installFactories()
        assertEquals(listOf("b3"), propagator.fields().toList())
    }

    @Test
    fun `b3 call captures result and buildPropagator returns it`() {
        val captured = dsl.b3(B3Format.SINGLE)
        assertSame(captured, dsl.buildPropagator())
    }

    private fun installFactories() {
        dsl.installFactories(
            traceFlagsFactory = traceFlagsFactory,
            traceStateFactory = traceStateFactory,
            spanContextFactory = spanContextFactory,
            spanFactory = spanFactory,
        )
    }
}
