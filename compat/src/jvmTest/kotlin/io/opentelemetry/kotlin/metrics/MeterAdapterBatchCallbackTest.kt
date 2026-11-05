package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaBatchCallback
import io.opentelemetry.kotlin.aliases.OtelJavaMeter
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.aliases.OtelJavaObservableMeasurement
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class MeterAdapterBatchCallbackTest {

    @Test
    fun registersDeclaredMeasurementsAndClosesRegistration() {
        val javaMeter = RecordingJavaMeter()
        val longMeasurement = RecordingJavaLongMeasurement()
        val doubleMeasurement = RecordingJavaDoubleMeasurement()
        val meter = MeterAdapter(javaMeter, NoopSdkErrorHandler)
        var calls = 0

        val registration = meter.batchCallback(
            ObservableLongMeasurementAdapter(longMeasurement),
            ObservableDoubleMeasurementAdapter(doubleMeasurement),
        ) { calls++ }

        assertEquals(listOf(longMeasurement, doubleMeasurement), javaMeter.measurements)
        javaMeter.callback?.run()
        assertEquals(1, calls)

        registration.close()
        assertTrue(javaMeter.closed)
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
        MeterAdapter(javaMeter, errorHandler).batchCallback(
            ObservableLongMeasurementAdapter(RecordingJavaLongMeasurement()),
        ) { error("callback failed") }

        javaMeter.callback?.run()

        assertEquals("callback failed", errorHandler.userCodeErrors.single().cause.message)
    }

    private class RecordingJavaMeter : OtelJavaMeter by OtelJavaMeterProvider.noop().get("") {
        var callback: Runnable? = null
        var measurements: List<OtelJavaObservableMeasurement> = emptyList()
        var closed = false

        override fun batchCallback(
            callback: Runnable,
            observableMeasurement: OtelJavaObservableMeasurement,
            vararg additionalMeasurements: OtelJavaObservableMeasurement,
        ): OtelJavaBatchCallback {
            this.callback = callback
            measurements = listOf(observableMeasurement) + additionalMeasurements
            return object : OtelJavaBatchCallback {
                override fun close() {
                    closed = true
                }
            }
        }
    }

    private class RecordingJavaLongMeasurement : OtelJavaObservableLongMeasurement {
        override fun record(value: Long) = Unit
        override fun record(value: Long, attributes: OtelJavaAttributes) = Unit
    }

    private class RecordingJavaDoubleMeasurement : OtelJavaObservableDoubleMeasurement {
        override fun record(value: Double) = Unit
        override fun record(value: Double, attributes: OtelJavaAttributes) = Unit
    }
}
