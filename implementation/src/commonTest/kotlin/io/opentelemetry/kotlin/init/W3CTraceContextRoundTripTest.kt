package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.config.dsl.PropagatorConfigImpl
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.propagation.FakeTextMapGetter
import io.opentelemetry.kotlin.propagation.FakeTextMapSetter
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class W3CTraceContextRoundTripTest {

    private val contextWithSpan = rootContext().storeSpanContext(FakeSpanContext.VALID)

    @Test
    fun `w3cTraceContext injects and extracts`() {
        val propagator = PropagatorConfigImpl().w3cTraceContext()
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())

        val carrier = mutableMapOf<String, String>()
        propagator.inject(contextWithSpan, carrier, FakeTextMapSetter)
        assertTrue(carrier.contains("traceparent"))

        val result = propagator.extract(
            context = rootContext(),
            carrier = mapOf("traceparent" to "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"),
            getter = FakeTextMapGetter
        )
        assertNotSame(rootContext(), result)
    }
}
