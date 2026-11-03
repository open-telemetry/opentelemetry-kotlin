package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopObservableLongGaugeTest {

    @Test
    fun ignoresCallbacksAndPreservesInstrumentMetadata() {
        var called = false
        val gauge = NoopMeter.createObservableLongGauge(
            name = "queue.depth",
            unit = "{item}",
            description = "Current queue depth",
            callbacks = arrayOf({ called = true }),
        )

        gauge.registerCallback { called = true }.close()
        gauge.close()

        assertEquals("queue.depth", gauge.name)
        assertEquals("{item}", gauge.unit)
        assertEquals("Current queue depth", gauge.description)
        assertFalse(called)
    }
}
