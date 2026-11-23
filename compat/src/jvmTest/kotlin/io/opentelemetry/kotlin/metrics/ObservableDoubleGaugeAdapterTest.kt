package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableDoubleGaugeAdapterTest {

    private val reader = FakeOtelJavaMetricReader()
    private val meter = MeterProviderAdapter(
        OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build(),
        NoopSdkErrorHandler,
    ).getMeter("test")

    @Test
    fun creationAndRegisteredCallbacksRecordMeasurements() {
        val initialCallback: (ObservableDoubleMeasurement) -> Unit = {
            it.record(21.5) { setStringAttribute("source", "initial") }
        }
        val gauge = meter.createObservableDoubleGauge(
            name = "temperature",
            unit = "Cel",
            description = "Current temperature",
            callbacks = arrayOf(initialCallback),
        )
        val registration = gauge.registerCallback {
            it.record(22.0) { setStringAttribute("source", "registered") }
        }

        val metric = reader.collect().single()
        assertEquals("temperature", gauge.name)
        assertEquals("Cel", gauge.unit)
        assertEquals("Current temperature", gauge.description)
        assertEquals("temperature", metric.name)
        assertEquals("Cel", metric.unit)
        assertEquals("Current temperature", metric.description)
        assertEquals(listOf(21.5, 22.0), metric.doubleGaugeData.points.map { it.value }.sorted())

        registration.close()
        gauge.close()
    }

    @Test
    fun closingRegistrationAndGaugeStopsCallbacks() {
        val gauge = meter.createObservableDoubleGauge("temperature")
        var calls = 0
        val registration = gauge.registerCallback {
            calls++
            it.record(21.5)
        }

        reader.collect()
        assertEquals(1, calls)

        registration.close()
        registration.close()
        reader.collect()
        assertEquals(1, calls)

        gauge.registerCallback {
            calls++
            it.record(22.0)
        }
        reader.collect()
        assertEquals(2, calls)

        gauge.close()
        gauge.close()
        gauge.registerCallback {
            calls++
            it.record(23.0)
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
        meter.createObservableDoubleGauge(
            name = "temperature",
            callbacks = arrayOf<(ObservableDoubleMeasurement) -> Unit>({ error("boom") }, { it.record(21.5) }),
        )

        assertEquals(listOf(21.5), reader.collect().single().doubleGaugeData.points.map { it.value })
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("ObservableDoubleGauge callback failed", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }
}
