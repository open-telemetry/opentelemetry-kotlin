package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopObservableDoubleGaugeTest {

    @Test
    fun ignoresCallbacksAndExposesPlaceholderMetadata() {
        var called = false
        val gauge = NoopMeter.createObservableDoubleGauge(
            name = "temperature",
            unit = "Cel",
            description = "Current temperature",
            callbacks = arrayOf({ called = true }),
        )

        gauge.registerCallback { called = true }.close()
        gauge.close()

        assertEquals("", gauge.name)
        assertEquals(null, gauge.unit)
        assertEquals(null, gauge.description)
        assertFalse(called)
    }
}
