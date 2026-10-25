package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeObservableDoubleCounterTest {

    @Test
    fun invokesCreationAndRegisteredCallbacksAndUnregistersThem() {
        val meter = FakeMeter("test")
        val values = mutableListOf<Double>()
        val creationCallback: (ObservableDoubleMeasurement) -> Unit = { measurement ->
            measurement.record(1.5)
        }
        val counter = meter.createObservableDoubleCounter(
            name = "process.cpu.time",
            unit = "s",
            description = "CPU time",
            callbacks = arrayOf(creationCallback),
        ) as FakeObservableDoubleCounter
        val measurement = FakeObservableDoubleMeasurement()
        val handle = counter.registerCallback {
            values.add(2.5)
            it.record(2.5)
        }

        counter.invokeCallbacks(measurement)
        handle.close()
        counter.invokeCallbacks(measurement)

        assertEquals("process.cpu.time", counter.name)
        assertEquals("s", counter.unit)
        assertEquals("CPU time", counter.description)
        assertEquals(listOf(1.5, 2.5, 1.5), measurement.adds.map { it.first })
        assertEquals(listOf(2.5), values)
        assertEquals(1, meter.observableDoubleCounters.size)

        counter.close()
        assertTrue(counter.callbacks.isEmpty())
        counter.invokeCallbacks(measurement)
        assertEquals(listOf(1.5, 2.5, 1.5), measurement.adds.map { it.first })
    }
}
