package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class NoopMeterTest {

    @Test
    fun reusesInstrumentsRegardlessOfMeterOrMetadata() {
        val firstMeter = NoopOpenTelemetry.meterProvider.getMeter("first")
        val secondMeter = NoopOpenTelemetry.meterProvider.getMeter("second")
        val first = instruments(firstMeter, "first.instrument", "By", "First description")
        val second = instruments(secondMeter, "second.instrument", "s", "Second description")
        val invalid = instruments(secondMeter, "1 invalid name", null, null)

        first.forEach { (kind, instrument) ->
            assertSame(instrument, second.getValue(kind), kind)
            assertSame(instrument, invalid.getValue(kind), kind)
        }
    }

    @Test
    fun exposesPlaceholderMetadata() {
        val meter = NoopOpenTelemetry.meterProvider.getMeter("test")

        instruments(meter, "instrument", "By", "Description").forEach { (kind, instrument) ->
            assertEquals("", instrument.name, kind)
            assertNull(instrument.unit, kind)
            assertNull(instrument.description, kind)
        }
    }

    @Test
    fun ignoresSynchronousMeasurementsAndAttributeCallbacks() {
        val meter = NoopOpenTelemetry.meterProvider.getMeter("test")
        val longGauge = meter.createLongGauge("long.gauge")
        val doubleGauge = meter.createDoubleGauge("double.gauge")
        val longCounter = meter.createLongUpDownCounter("long.counter")
        val doubleCounter = meter.createDoubleUpDownCounter("double.counter")

        assertFalse(longGauge.enabled())
        assertFalse(doubleGauge.enabled())
        assertFalse(longCounter.enabled())
        assertFalse(doubleCounter.enabled())
        longGauge.record(Long.MIN_VALUE) { error("Attributes must not be evaluated") }
        doubleGauge.record(Double.NaN) { error("Attributes must not be evaluated") }
        longCounter.add(Long.MAX_VALUE) { error("Attributes must not be evaluated") }
        doubleCounter.add(Double.POSITIVE_INFINITY) { error("Attributes must not be evaluated") }
    }

    @Test
    fun closingSharedInstrumentsLeavesMeasurementHandlesUsable() {
        val meter = NoopOpenTelemetry.meterProvider.getMeter("test")
        val longInstruments = listOf(
            meter.createObservableLongCounter("counter"),
            meter.createObservableLongUpDownCounter("up_down_counter"),
            meter.createObservableLongGauge("gauge"),
        )
        val doubleInstruments = listOf(
            meter.createObservableDoubleCounter("counter"),
            meter.createObservableDoubleUpDownCounter("up_down_counter"),
            meter.createObservableDoubleGauge("gauge"),
        )

        longInstruments.forEach { instrument ->
            val measurement = instrument.measurement
            val handle = instrument.registerCallback { error("Callback must not be invoked") }
            handle.close()
            handle.close()
            instrument.close()
            instrument.close()
            assertSame(measurement, instrument.measurement)
            measurement.record(Long.MIN_VALUE) { error("Attributes must not be evaluated") }
            instrument.registerCallback { error("Callback must not be invoked") }.close()
        }
        doubleInstruments.forEach { instrument ->
            val measurement = instrument.measurement
            val handle = instrument.registerCallback { error("Callback must not be invoked") }
            handle.close()
            handle.close()
            instrument.close()
            instrument.close()
            assertSame(measurement, instrument.measurement)
            measurement.record(Double.NaN) { error("Attributes must not be evaluated") }
            instrument.registerCallback { error("Callback must not be invoked") }.close()
        }
    }

    @Test
    fun sharesStatelessInstrumentsAcrossConcurrentCallers() = runTest {
        val meter = NoopOpenTelemetry.meterProvider.getMeter("test")
        val expected = instruments(meter, "first", "By", "First description")

        List(8) { caller ->
            async(Dispatchers.Default) {
                val actual = instruments(meter, "instrument.$caller", "s", "Caller $caller")
                actual.forEach { (kind, instrument) ->
                    assertSame(expected.getValue(kind), instrument, kind)
                    assertEquals("", instrument.name, kind)
                    if (instrument is AsynchronousInstrument<*>) {
                        instrument.close()
                    }
                }
                meter.createLongGauge("gauge.$caller").record(caller.toLong()) {
                    error("Attributes must not be evaluated")
                }
            }
        }.awaitAll()
    }

    private fun instruments(
        meter: Meter,
        name: String,
        unit: String?,
        description: String?,
    ): Map<String, Instrument> = mapOf(
        "long gauge" to meter.createLongGauge(name, unit, description),
        "double gauge" to meter.createDoubleGauge(name, unit, description),
        "long up-down counter" to meter.createLongUpDownCounter(name, unit, description),
        "double up-down counter" to meter.createDoubleUpDownCounter(name, unit, description),
        "observable long counter" to meter.createObservableLongCounter(name, unit, description),
        "observable double counter" to meter.createObservableDoubleCounter(name, unit, description),
        "observable long up-down counter" to meter.createObservableLongUpDownCounter(name, unit, description),
        "observable double up-down counter" to meter.createObservableDoubleUpDownCounter(name, unit, description),
        "observable long gauge" to meter.createObservableLongGauge(name, unit, description),
        "observable double gauge" to meter.createObservableDoubleGauge(name, unit, description),
    )
}
