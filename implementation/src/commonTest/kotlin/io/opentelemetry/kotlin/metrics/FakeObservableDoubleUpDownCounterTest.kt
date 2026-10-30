package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeObservableDoubleUpDownCounterTest {

    @Test
    fun invokesCreationAndRegisteredCallbacksAndUnregistersThem() {
        val meter = FakeMeter("test")
        val creationCallback: (ObservableDoubleMeasurement) -> Unit = { measurement ->
            measurement.record(1.5)
        }
        val counter = meter.createObservableDoubleUpDownCounter(
            name = "process.cpu.load",
            unit = "1",
            description = "Process CPU load",
            callbacks = arrayOf(creationCallback),
        ) as FakeObservableDoubleUpDownCounter
        val measurement = FakeObservableDoubleMeasurement()
        val handle = counter.registerCallback { it.record(-0.5) }

        counter.invokeCallbacks(measurement)
        handle.close()
        counter.invokeCallbacks(measurement)

        assertEquals("process.cpu.load", counter.name)
        assertEquals("1", counter.unit)
        assertEquals("Process CPU load", counter.description)
        assertEquals(listOf(1.5, -0.5, 1.5), measurement.adds.map { it.first })
        assertEquals(1, meter.observableDoubleUpDownCounters.size)

        counter.close()
        assertTrue(counter.callbacks.isEmpty())
        counter.invokeCallbacks(measurement)
        assertEquals(listOf(1.5, -0.5, 1.5), measurement.adds.map { it.first })
    }
}
