package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeObservableDoubleGaugeTest {

    @Test
    fun invokesCreationAndRegisteredCallbacksAndUnregistersThem() {
        val meter = FakeMeter("test")
        val values = mutableListOf<Double>()
        val creationCallback: (ObservableDoubleMeasurement) -> Unit = { measurement ->
            measurement.record(21.5)
        }
        val gauge = meter.createObservableDoubleGauge(
            name = "temperature",
            unit = "Cel",
            description = "Current temperature",
            callbacks = arrayOf(creationCallback),
        ) as FakeObservableDoubleGauge
        val measurement = FakeObservableDoubleMeasurement()
        val handle = gauge.registerCallback {
            values.add(22.0)
            it.record(22.0)
        }

        gauge.invokeCallbacks(measurement)
        handle.close()
        gauge.invokeCallbacks(measurement)

        assertEquals("temperature", gauge.name)
        assertEquals("Cel", gauge.unit)
        assertEquals("Current temperature", gauge.description)
        assertEquals(listOf(21.5, 22.0, 21.5), measurement.adds.map { it.first })
        assertEquals(listOf(22.0), values)
        assertEquals(1, meter.observableDoubleGauges.size)

        gauge.close()
        assertTrue(gauge.callbacks.isEmpty())
        gauge.invokeCallbacks(measurement)
        assertEquals(listOf(21.5, 22.0, 21.5), measurement.adds.map { it.first })
    }
}
