package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaMeterAdapterBatchCallbackTest {

    @Test
    fun registersDeclaredMeasurementsAndClosesRegistration() {
        val meter = FakeMeter("test-meter")
        val longMeasurement = FakeObservableLongMeasurement()
        val doubleMeasurement = FakeObservableDoubleMeasurement()
        val adapter = OtelJavaMeterAdapter(meter)
        var calls = 0

        val registration = adapter.batchCallback(
            Runnable { calls++ },
            OtelJavaObservableLongMeasurementAdapter(longMeasurement),
            OtelJavaObservableDoubleMeasurementAdapter(doubleMeasurement),
        )

        assertEquals(listOf(longMeasurement, doubleMeasurement), meter.batchCallbacks.single().measurements)
        meter.batchCallbacks.single().collect()
        assertEquals(1, calls)

        registration.close()
        assertTrue(meter.batchCallbacks.isEmpty())
    }

    @Test
    fun unsupportedMeasurementDoesNotRegisterCallback() {
        val meter = FakeMeter("test-meter")
        val errorHandler = FakeSdkErrorHandler()
        val adapter = OtelJavaMeterAdapter(meter, sdkErrorHandler = errorHandler)
        val javaMeasurement = object : OtelJavaObservableLongMeasurement {
            override fun record(value: Long) = Unit
            override fun record(value: Long, attributes: OtelJavaAttributes) = Unit
        }

        val registration = adapter.batchCallback(Runnable { error("Must not be called") }, javaMeasurement)

        assertEquals(1, errorHandler.apiMisuses.size)
        assertTrue(meter.batchCallbacks.isEmpty())
        registration.close()
    }

    @Test
    fun callbackFailureIsReportedWithoutEscaping() {
        val meter = FakeMeter("test-meter")
        val errorHandler = FakeSdkErrorHandler()
        val adapter = OtelJavaMeterAdapter(meter, sdkErrorHandler = errorHandler)
        adapter.batchCallback(
            { error("callback failed") },
            OtelJavaObservableLongMeasurementAdapter(FakeObservableLongMeasurement()),
        )

        meter.batchCallbacks.single().collect()

        assertEquals("callback failed", errorHandler.userCodeErrors.single().cause.message)
    }
}
