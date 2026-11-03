package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeObservableLongUpDownCounterTest {

    @Test
    fun invokesCreationAndRegisteredCallbacksAndUnregistersThem() {
        val meter = FakeMeter("test")
        val creationCallback: (ObservableLongMeasurement) -> Unit = { measurement ->
            measurement.record(1L)
        }
        val counter = meter.createObservableLongUpDownCounter(
            name = "process.memory.working_set",
            unit = "By",
            description = "Process working set size",
            callbacks = arrayOf(creationCallback),
        ) as FakeObservableLongUpDownCounter
        val measurement = FakeObservableLongMeasurement()
        val handle = counter.registerCallback { it.record(-2L) }

        counter.invokeCallbacks(measurement)
        handle.close()
        counter.invokeCallbacks(measurement)

        assertEquals("process.memory.working_set", counter.name)
        assertEquals("By", counter.unit)
        assertEquals("Process working set size", counter.description)
        assertEquals(listOf(1L, -2L, 1L), measurement.adds.map { it.first })
        assertEquals(1, meter.observableLongUpDownCounters.size)

        counter.close()
        assertTrue(counter.callbacks.isEmpty())
        counter.invokeCallbacks(measurement)
        assertEquals(listOf(1L, -2L, 1L), measurement.adds.map { it.first })
    }
}
