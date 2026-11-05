package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableLongGaugeAdapterTest {

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
        val gauge = meter.createObservableLongGauge(
            name = "queue.depth",
            unit = "{item}",
            description = "Current queue depth",
            callbacks = arrayOf(initialCallback),
        )
        val registration = gauge.registerCallback {
            it.record(2L) { setStringAttribute("source", "registered") }
        }

        val metric = reader.collect().single()
        assertEquals("queue.depth", gauge.name)
        assertEquals("{item}", gauge.unit)
        assertEquals("Current queue depth", gauge.description)
        assertEquals("queue.depth", metric.name)
        assertEquals("{item}", metric.unit)
        assertEquals("Current queue depth", metric.description)
        assertEquals(listOf(1L, 2L), metric.longGaugeData.points.map { it.value }.sorted())

        registration.close()
        gauge.close()
    }

    @Test
    fun closingRegistrationAndGaugeStopsCallbacks() {
        val gauge = meter.createObservableLongGauge("queue.depth")
        var calls = 0
        val registration = gauge.registerCallback {
            calls++
            it.record(1L)
        }

        reader.collect()
        assertEquals(1, calls)

        registration.close()
        registration.close()
        reader.collect()
        assertEquals(1, calls)

        gauge.registerCallback {
            calls++
            it.record(2L)
        }
        reader.collect()
        assertEquals(2, calls)

        gauge.close()
        gauge.close()
        gauge.registerCallback {
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
        meter.createObservableLongGauge(
            name = "queue.depth",
            callbacks = arrayOf<(ObservableLongMeasurement) -> Unit>({ error("boom") }, { it.record(1L) }),
        )

        assertEquals(listOf(1L), reader.collect().single().longGaugeData.points.map { it.value })
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("ObservableLongGauge callback failed", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }
}
