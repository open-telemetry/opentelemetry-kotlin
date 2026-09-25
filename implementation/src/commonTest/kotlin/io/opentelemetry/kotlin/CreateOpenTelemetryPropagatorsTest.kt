package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.propagation.FakeTextMapPropagator
import io.opentelemetry.kotlin.propagation.W3CBaggagePropagator
import io.opentelemetry.kotlin.propagation.createPropagators
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryPropagatorsTest {

    @Test
    fun `default propagators are used when none are supplied`() {
        assertSame(createPropagators().none(), createOpenTelemetry().propagator)
    }

    @Test
    fun `supplied propagators are used when the DSL configures nothing`() {
        val none = FakeTextMapPropagator()
        val api = createOpenTelemetry(propagators = FakePropagators(none))
        assertSame(none, api.propagator)
    }

    @Test
    fun `supplied propagators do not override an explicitly configured propagator`() {
        val api = createOpenTelemetry(propagators = FakePropagators(FakeTextMapPropagator())) {
            propagator { w3cBaggage() }
        }
        assertSame(W3CBaggagePropagator, api.propagator)
    }
}
