package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableLongUpDownCounterAdapterTest {

    private val reader = FakeOtelJavaMetricReader()
    private val meter = MeterProviderAdapter(
        OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build(),
        NoopSdkErrorHandler,
    ).getMeter("test")

    @Test
    fun creationAndRegisteredCallbacksRecordMeasurements() {
        val counter = meter.createObservableLongUpDownCounter(
            name = "process.memory.working_set",
            unit = "By",
            description = "Process working set size",
            callbacks = arrayOf({ measurement -> measurement.record(4L) }),
        )
        val registration = counter.registerCallback { measurement -> measurement.record(-2L) }

        val metric = reader.collect().single()
        assertEquals("process.memory.working_set", counter.name)
        assertEquals("By", counter.unit)
        assertEquals("Process working set size", counter.description)
        assertEquals("process.memory.working_set", metric.name)
        assertEquals("By", metric.unit)
        assertEquals("Process working set size", metric.description)
        assertEquals(listOf(2L), metric.longSumData.points.map { it.value })

        registration.close()
        assertEquals(listOf(4L), reader.collect().single().longSumData.points.map { it.value })
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
        meter.createObservableLongUpDownCounter(
            name = "process.memory.working_set",
            callbacks = arrayOf<(ObservableLongMeasurement) -> Unit>({ error("boom") }, { it.record(-1L) }),
        )

        assertEquals(listOf(-1L), reader.collect().single().longSumData.points.map { it.value })
        val recorded = errorHandler.userCodeErrors.single()
        assertEquals("ObservableLongUpDownCounter callback failed", recorded.message)
        assertEquals("boom", recorded.cause.message)
    }
}
