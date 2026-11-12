package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaMeterAdapterBatchCallbackTest {

    @Test
    fun registersDeclaredMeasurementsAndClosesRegistration() {
        val meter = FakeMeter("test-meter")
        val adapter = OtelJavaMeterAdapter(meter)
        val longCounter = adapter.counterBuilder("long.counter").buildObserver()
        val doubleCounter = adapter.counterBuilder("double.counter").ofDoubles().buildObserver()
        val longUpDownCounter = adapter.upDownCounterBuilder("long.up_down_counter").buildObserver()
        val doubleUpDownCounter = adapter.upDownCounterBuilder("double.up_down_counter").ofDoubles().buildObserver()
        val longGauge = adapter.gaugeBuilder("long.gauge").ofLongs().buildObserver()
        val doubleGauge = adapter.gaugeBuilder("double.gauge").buildObserver()

        val registration = adapter.batchCallback(
            Runnable {
                longCounter.record(3)
                doubleCounter.record(4.5)
                longUpDownCounter.record(-2)
                doubleUpDownCounter.record(-0.5)
                longGauge.record(7)
                doubleGauge.record(8.5)
            },
            longCounter,
            doubleCounter,
            longUpDownCounter,
            doubleUpDownCounter,
            longGauge,
            doubleGauge,
        )

        val batch = meter.batchCallbacks.single()
        assertEquals(
            listOf(
                meter.observableLongCounters.single().measurement,
                meter.observableDoubleCounters.single().measurement,
                meter.observableLongUpDownCounters.single().measurement,
                meter.observableDoubleUpDownCounters.single().measurement,
                meter.observableLongGauges.single().measurement,
                meter.observableDoubleGauges.single().measurement,
            ),
            batch.measurements,
        )
        batch.collect()
        assertEquals(listOf(3L), meter.observableLongCounters.single().measurement.adds.map { it.first })
        assertEquals(listOf(4.5), meter.observableDoubleCounters.single().measurement.adds.map { it.first })
        assertEquals(listOf(-2L), meter.observableLongUpDownCounters.single().measurement.adds.map { it.first })
        assertEquals(listOf(-0.5), meter.observableDoubleUpDownCounters.single().measurement.adds.map { it.first })
        assertEquals(listOf(7L), meter.observableLongGauges.single().measurement.adds.map { it.first })
        assertEquals(listOf(8.5), meter.observableDoubleGauges.single().measurement.adds.map { it.first })

        registration.close()
        assertTrue(meter.batchCallbacks.isEmpty())
        batch.collect()
        assertEquals(1, meter.observableLongCounters.single().measurement.adds.size)
    }

    @Test
    fun unsupportedMeasurementDoesNotRegisterCallback() {
        val meter = FakeMeter("test-meter")
        val errorHandler = FakeSdkErrorHandler()
        val adapter = OtelJavaMeterAdapter(meter, sdkErrorHandler = errorHandler)
        val javaMeasurement = object : OtelJavaObservableLongMeasurement {
            override fun record(value: Long) = Unit
            override fun record(value: Long, attributes: OtelJavaAttributes) = Unit
        }

        val registration = adapter.batchCallback(Runnable { error("Must not be called") }, javaMeasurement)

        assertEquals(1, errorHandler.apiMisuses.size)
        assertTrue(meter.batchCallbacks.isEmpty())
        registration.close()
    }

    @Test
    fun callbackFailureIsReportedWithoutEscaping() {
        val meter = FakeMeter("test-meter")
        val errorHandler = FakeSdkErrorHandler()
        val adapter = OtelJavaMeterAdapter(meter, sdkErrorHandler = errorHandler)
        val measurement = adapter.counterBuilder("long.counter").buildObserver()
        adapter.batchCallback(
            { error("callback failed") },
            measurement,
        )

        meter.batchCallbacks.single().collect()

        assertEquals("callback failed", errorHandler.userCodeErrors.single().cause.message)
    }
}
