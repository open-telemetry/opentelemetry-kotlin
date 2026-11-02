package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableLongCounterAdapterTest {

    private val reader = FakeOtelJavaMetricReader()
    private val meter = MeterProviderAdapter(
        OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build(),
        NoopSdkErrorHandler,
    ).getMeter("test")

    @Test
    fun creationAndRegisteredCallbacksRecordMeasurements() {
        val initialCallback: (ObservableLongMeasurement) -> Unit = {
            it.record(1L) { setStringAttribute("source", "initial") }
        }
        val counter = meter.createObservableLongCounter(
            name = "process.page.faults",
            unit = "{fault}",
            description = "Page faults",
            callbacks = arrayOf(initialCallback),
        )
        val registration = counter.registerCallback {
            it.record(2L) { setStringAttribute("source", "registered") }
        }

        val metric = reader.collect().single()
        assertEquals("process.page.faults", counter.name)
        assertEquals("{fault}", counter.unit)
        assertEquals("Page faults", counter.description)
        assertEquals("process.page.faults", metric.name)
        assertEquals("{fault}", metric.unit)
        assertEquals("Page faults", metric.description)
        assertEquals(listOf(1L, 2L), metric.longSumData.points.map { it.value }.sorted())

        registration.close()
        counter.close()
    }

    @Test
    fun closingRegistrationAndCounterStopsCallbacks() {
        val counter = meter.createObservableLongCounter("process.page.faults")
        var calls = 0
        val registration = counter.registerCallback {
            calls++
            it.record(1L)
        }

        reader.collect()
        assertEquals(1, calls)

        registration.close()
        registration.close()
        reader.collect()
        assertEquals(1, calls)

        counter.registerCallback {
            calls++
            it.record(2L)
        }
        reader.collect()
        assertEquals(2, calls)

        counter.close()
        counter.close()
        counter.registerCallback {
            calls++
            it.record(3L)
        }.close()
        reader.collect()
        assertEquals(2, calls)
    }

    @Test
    fun throwingCallbackIsReported() {
        val errorHandler = FakeSdkErrorHandler()
        val reader = FakeOtelJavaMetricReader()
        val meter = MeterProviderAdapter(
            OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build(),
            errorHandler,
        ).getMeter("test")
        meter.createObservableLongCounter(
            name = "process.page.faults",
            callbacks = arrayOf<(ObservableLongMeasurement) -> Unit>({ error("boom") }, { it.record(1L) }),
        )

        assertEquals(listOf(1L), reader.collect().single().longSumData.points.map { it.value })
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("ObservableLongCounter callback failed", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }
}
