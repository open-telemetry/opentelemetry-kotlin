package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.propagation.createNoopPropagator
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryPropagatorsTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `noop propagator is used when the DSL configures nothing`() {
        assertSame(createNoopPropagator(), createOpenTelemetry().also(sdks::track).propagator)
    }
}
