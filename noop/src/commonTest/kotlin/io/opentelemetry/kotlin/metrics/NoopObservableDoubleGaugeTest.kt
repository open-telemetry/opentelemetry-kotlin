package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopObservableDoubleGaugeTest {

    @Test
    fun ignoresCallbacksAndPreservesInstrumentMetadata() {
        var called = false
        val gauge = NoopMeter.createObservableDoubleGauge(
            name = "temperature",
            unit = "Cel",
            description = "Current temperature",
            callbacks = arrayOf({ called = true }),
        )

        gauge.registerCallback { called = true }.close()
        gauge.close()

        assertEquals("temperature", gauge.name)
        assertEquals("Cel", gauge.unit)
        assertEquals("Current temperature", gauge.description)
        assertFalse(called)
    }
}
