package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableDoubleUpDownCounterAdapterTest {

    private val reader = FakeOtelJavaMetricReader()
    private val meter = MeterProviderAdapter(
        OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build(),
        NoopSdkErrorHandler,
    ).getMeter("test")

    @Test
    fun creationAndRegisteredCallbacksRecordMeasurements() {
        val counter = meter.createObservableDoubleUpDownCounter(
            name = "process.cpu.load",
            unit = "1",
            description = "Process CPU load",
            callbacks = arrayOf({ measurement -> measurement.record(1.5) }),
        )
        val registration = counter.registerCallback { measurement -> measurement.record(-0.5) }

        val metric = reader.collect().single()
        assertEquals("process.cpu.load", counter.name)
        assertEquals("1", counter.unit)
        assertEquals("Process CPU load", counter.description)
        assertEquals("process.cpu.load", metric.name)
        assertEquals("1", metric.unit)
        assertEquals("Process CPU load", metric.description)
        assertEquals(listOf(1.0), metric.doubleSumData.points.map { it.value })

        registration.close()
        assertEquals(listOf(1.5), reader.collect().single().doubleSumData.points.map { it.value })
        counter.close()
        reader.collect()
    }

    @Test
    fun throwingCallbackIsReported() {
        val errorHandler = FakeSdkErrorHandler()
        val reader = FakeOtelJavaMetricReader()
        val meter = MeterProviderAdapter(
            OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build(),
            errorHandler,
        ).getMeter("test")
        meter.createObservableDoubleUpDownCounter(
            name = "process.cpu.load",
            callbacks = arrayOf({ error("boom") }, { it.record(-0.5) }),
        )

        assertEquals(listOf(-0.5), reader.collect().single().doubleSumData.points.map { it.value })
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("ObservableDoubleUpDownCounter callback failed", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }
}
