package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleGaugeBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaLongCounterBuilder
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class ObservableMeasurementHandleAdapterTest {

    @Test
    fun longCounterHandleBuildsOnceAndRecordsThroughJavaObserver() {
        val javaMeter = RecordingJavaMeter()
        val counter = MeterAdapter(javaMeter, NoopSdkErrorHandler).createObservableLongCounter(
            "jobs",
            "{job}",
            "Completed jobs",
        )

        assertEquals(0, javaMeter.longObserverBuilds)
        val measurement = counter.measurement
        assertSame(measurement, counter.measurement)
        measurement.record(3)

        assertEquals(1, javaMeter.longObserverBuilds)
        assertEquals("jobs", javaMeter.longName)
        assertEquals("{job}", javaMeter.longUnit)
        assertEquals("Completed jobs", javaMeter.longDescription)
        assertEquals(listOf(3L), javaMeter.longValues)
    }

    @Test
    fun doubleGaugeHandleBuildsOnceAndRecordsThroughJavaObserver() {
        val javaMeter = RecordingJavaMeter()
        val gauge = MeterAdapter(javaMeter, NoopSdkErrorHandler).createObservableDoubleGauge(
            "temperature",
            "Cel",
            "Current temperature",
        )

        assertEquals(0, javaMeter.doubleObserverBuilds)
        val measurement = gauge.measurement
        assertSame(measurement, gauge.measurement)
        measurement.record(21.5)

        assertEquals(1, javaMeter.doubleObserverBuilds)
        assertEquals("temperature", javaMeter.doubleName)
        assertEquals("Cel", javaMeter.doubleUnit)
        assertEquals("Current temperature", javaMeter.doubleDescription)
        assertEquals(listOf(21.5), javaMeter.doubleValues)
    }

    @Test
    fun allObserverHandlesCanBeBuiltByTheJavaSdk() {
        val errorHandler = FakeSdkErrorHandler()
        val meter = MeterAdapter(OtelJavaSdkMeterProvider.builder().build().get("test"), errorHandler)
        val longInstruments = listOf(
            meter.createObservableLongCounter("long.counter"),
            meter.createObservableLongUpDownCounter("long.up_down_counter"),
            meter.createObservableLongGauge("long.gauge"),
        )
        val doubleInstruments = listOf(
            meter.createObservableDoubleCounter("double.counter"),
            meter.createObservableDoubleUpDownCounter("double.up_down_counter"),
            meter.createObservableDoubleGauge("double.gauge"),
        )

        longInstruments.forEach { assertSame(it.measurement, it.measurement) }
        doubleInstruments.forEach { assertSame(it.measurement, it.measurement) }
        assertTrue(errorHandler.sdkCodeErrors.isEmpty())
    }

    @Test
    fun observersWithMetadataCanBeBuiltByTheJavaSdk() {
        val errorHandler = FakeSdkErrorHandler()
        val meter = MeterAdapter(OtelJavaSdkMeterProvider.builder().build().get("test"), errorHandler)

        assertIs<ObservableDoubleMeasurementAdapter>(
            meter.createObservableDoubleCounter("double.counter", "1", "Double counter").measurement,
        )
        assertIs<ObservableDoubleMeasurementAdapter>(
            meter.createObservableDoubleUpDownCounter("double.up_down_counter", "{item}", "Queue size").measurement,
        )
        assertIs<ObservableLongMeasurementAdapter>(
            meter.createObservableLongGauge("long.gauge", "By", "Memory usage").measurement,
        )
        assertIs<ObservableLongMeasurementAdapter>(
            meter.createObservableLongUpDownCounter("long.up_down_counter", "{item}", "Queue size").measurement,
        )
        assertTrue(errorHandler.sdkCodeErrors.isEmpty())
    }

    private class RecordingJavaMeter : OtelJavaMeter by OtelJavaMeterProvider.noop().get("test") {
        private val noopMeter = OtelJavaMeterProvider.noop().get("test")

        var longName: String? = null
        var longUnit: String? = null
        var longDescription: String? = null
        var longObserverBuilds = 0
        val longValues = mutableListOf<Long>()

        var doubleName: String? = null
        var doubleUnit: String? = null
        var doubleDescription: String? = null
        var doubleObserverBuilds = 0
        val doubleValues = mutableListOf<Double>()

        override fun counterBuilder(name: String): OtelJavaLongCounterBuilder =
            object : OtelJavaLongCounterBuilder by noopMeter.counterBuilder(name) {
                init { longName = name }

                override fun setUnit(unit: String): OtelJavaLongCounterBuilder = this.also { longUnit = unit }

                override fun setDescription(description: String): OtelJavaLongCounterBuilder =
                    this.also { longDescription = description }

                override fun buildObserver(): OtelJavaObservableLongMeasurement {
                    longObserverBuilds++
                    return object : OtelJavaObservableLongMeasurement {
                        override fun record(value: Long) { longValues.add(value) }
                        override fun record(value: Long, attributes: OtelJavaAttributes) { longValues.add(value) }
                    }
                }
            }

        override fun gaugeBuilder(name: String): OtelJavaDoubleGaugeBuilder =
            object : OtelJavaDoubleGaugeBuilder by noopMeter.gaugeBuilder(name) {
                init { doubleName = name }

                override fun setUnit(unit: String): OtelJavaDoubleGaugeBuilder = this.also { doubleUnit = unit }

                override fun setDescription(description: String): OtelJavaDoubleGaugeBuilder =
                    this.also { doubleDescription = description }

                override fun buildObserver(): OtelJavaObservableDoubleMeasurement {
                    doubleObserverBuilds++
                    return object : OtelJavaObservableDoubleMeasurement {
                        override fun record(value: Double) { doubleValues.add(value) }
                        override fun record(value: Double, attributes: OtelJavaAttributes) { doubleValues.add(value) }
                    }
                }
            }
    }
}
