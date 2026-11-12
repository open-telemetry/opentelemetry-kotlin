package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBatchCallback
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalApi::class)
internal class MeterAdapterBatchCallbackTest {

    @Test
    fun publicInstrumentsRegisterRealMeasurements() {
        val reader = FakeOtelJavaMetricReader()
        val provider = OtelJavaSdkMeterProvider.builder().registerMetricReader(reader).build()
        val meter = MeterAdapter(provider.get("test"), NoopSdkErrorHandler)
        val counter = meter.createObservableLongCounter("jobs").measurement
        val gauge = meter.createObservableDoubleGauge("capacity").measurement
        var calls = 0

        val registration = meter.batchCallback(counter, gauge) {
            calls++
            counter.record(5)
            gauge.record(6.5)
        }

        val metrics = reader.collect().associateBy { it.name }
        assertEquals(1, calls)
        assertEquals(listOf(5L), metrics.getValue("jobs").longSumData.points.map { it.value })
        assertEquals(listOf(6.5), metrics.getValue("capacity").doubleGaugeData.points.map { it.value })

        registration.close()
        reader.collect()
        assertEquals(1, calls)
    }

    @Test
    fun unsupportedMeasurementDoesNotRegisterCallback() {
        val javaMeter = RecordingJavaMeter()
        val errorHandler = FakeSdkErrorHandler()
        val meter = MeterAdapter(javaMeter, errorHandler)

        val registration = meter.batchCallback(FakeObservableLongMeasurement()) {
            error("Must not be called")
        }

        assertEquals(1, errorHandler.apiMisuses.size)
        assertNull(javaMeter.callback)
        registration.close()
    }

    @Test
    fun callbackFailureIsReportedWithoutEscaping() {
        val javaMeter = RecordingJavaMeter()
        val errorHandler = FakeSdkErrorHandler()
        val meter = MeterAdapter(javaMeter, errorHandler)
        val measurement = meter.createObservableLongCounter("jobs").measurement
        meter.batchCallback(measurement) { error("callback failed") }

        javaMeter.callback?.run()

        assertEquals("callback failed", errorHandler.userCodeErrors.single().cause.message)
    }

    private class RecordingJavaMeter : OtelJavaMeter by OtelJavaMeterProvider.noop().get("") {
        var callback: Runnable? = null

        override fun batchCallback(
            callback: Runnable,
            observableMeasurement: OtelJavaObservableMeasurement,
            vararg additionalMeasurements: OtelJavaObservableMeasurement,
        ): OtelJavaBatchCallback {
            this.callback = callback
            return object : OtelJavaBatchCallback {
                override fun close() = Unit
            }
        }
    }
}
