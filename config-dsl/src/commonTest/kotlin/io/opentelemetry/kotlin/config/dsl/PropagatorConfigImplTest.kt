package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.FakeTextMapSetter
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
}
