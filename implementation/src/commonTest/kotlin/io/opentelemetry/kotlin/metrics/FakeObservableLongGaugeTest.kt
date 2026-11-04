package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeObservableLongGaugeTest {

    @Test
    fun invokesCreationAndRegisteredCallbacksAndUnregistersThem() {
        val meter = FakeMeter("test")
        val values = mutableListOf<Long>()
        val creationCallback: (ObservableLongMeasurement) -> Unit = { measurement ->
            measurement.record(1L)
        }
        val gauge = meter.createObservableLongGauge(
            name = "queue.depth",
            unit = "{item}",
            description = "Current queue depth",
            callbacks = arrayOf(creationCallback),
        ) as FakeObservableLongGauge
        val measurement = FakeObservableLongMeasurement()
        val handle = gauge.registerCallback {
            values.add(2L)
            it.record(2L)
        }

        gauge.invokeCallbacks(measurement)
        handle.close()
        gauge.invokeCallbacks(measurement)

        assertEquals("queue.depth", gauge.name)
        assertEquals("{item}", gauge.unit)
        assertEquals("Current queue depth", gauge.description)
        assertEquals(listOf(1L, 2L, 1L), measurement.adds.map { it.first })
        assertEquals(listOf(2L), values)
        assertEquals(1, meter.observableLongGauges.size)

        gauge.close()
        assertTrue(gauge.callbacks.isEmpty())
        gauge.invokeCallbacks(measurement)
        assertEquals(listOf(1L, 2L, 1L), measurement.adds.map { it.first })
    }
}
