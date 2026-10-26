package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableDoubleCounterAdapterTest {

    private val reader = FakeOtelJavaMetricReader()
    private val meter = MeterProviderAdapter(
        OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build(),
        NoopSdkErrorHandler,
    ).getMeter("test")

    @Test
    fun creationAndRegisteredCallbacksRecordMeasurements() {
        val initialCallback: (ObservableDoubleMeasurement) -> Unit = {
            it.record(1.5) { setStringAttribute("source", "initial") }
        }
        val counter = meter.createObservableDoubleCounter(
            name = "process.cpu.time",
            unit = "s",
            description = "CPU time",
            callbacks = arrayOf(initialCallback),
        )
        val registration = counter.registerCallback {
            it.record(2.5) { setStringAttribute("source", "registered") }
        }

        val metric = reader.collect().single()
        assertEquals("process.cpu.time", counter.name)
        assertEquals("s", counter.unit)
        assertEquals("CPU time", counter.description)
        assertEquals("process.cpu.time", metric.name)
        assertEquals("s", metric.unit)
        assertEquals("CPU time", metric.description)
        assertEquals(listOf(1.5, 2.5), metric.doubleSumData.points.map { it.value }.sorted())

        registration.close()
        counter.close()
    }

    @Test
    fun closingRegistrationAndCounterStopsCallbacks() {
        val counter = meter.createObservableDoubleCounter("process.cpu.time")
        var calls = 0
        val registration = counter.registerCallback {
            calls++
            it.record(1.0)
        }

        reader.collect()
        assertEquals(1, calls)

        registration.close()
        registration.close()
        reader.collect()
        assertEquals(1, calls)

        counter.registerCallback {
            calls++
            it.record(2.0)
        }
        reader.collect()
        assertEquals(2, calls)

        counter.close()
        counter.close()
        counter.registerCallback {
            calls++
            it.record(3.0)
        }.close()
        reader.collect()
        assertEquals(2, calls)
    }
}
