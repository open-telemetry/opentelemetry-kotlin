package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.propagation.createNoopPropagator
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryPropagatorsTest {

    @Test
    fun `noop propagator is used when the DSL configures nothing`() {
        assertSame(createNoopPropagator(), createOpenTelemetry().propagator)
    }
}
