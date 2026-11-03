package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeObservableLongCounterTest {

    @Test
    fun invokesCreationAndRegisteredCallbacksAndUnregistersThem() {
        val meter = FakeMeter("test")
        val values = mutableListOf<Long>()
        val creationCallback: (ObservableLongMeasurement) -> Unit = { measurement ->
            measurement.record(1L)
        }
        val counter = meter.createObservableLongCounter(
            name = "process.page.faults",
            unit = "{fault}",
            description = "Page faults",
            callbacks = arrayOf(creationCallback),
        ) as FakeObservableLongCounter
        val measurement = FakeObservableLongMeasurement()
        val handle = counter.registerCallback {
            values.add(2L)
            it.record(2L)
        }

        counter.invokeCallbacks(measurement)
        handle.close()
        counter.invokeCallbacks(measurement)

        assertEquals("process.page.faults", counter.name)
        assertEquals("{fault}", counter.unit)
        assertEquals("Page faults", counter.description)
        assertEquals(listOf(1L, 2L, 1L), measurement.adds.map { it.first })
        assertEquals(listOf(2L), values)
        assertEquals(1, meter.observableLongCounters.size)

        counter.close()
        assertTrue(counter.callbacks.isEmpty())
        counter.invokeCallbacks(measurement)
        assertEquals(listOf(1L, 2L, 1L), measurement.adds.map { it.first })
    }
}
