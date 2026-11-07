package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeBatchCallbackTest {

    @Test
    fun callbackUsesDeclaredMeasurementsUntilClosed() {
        val meter = FakeMeter("test-meter")
        val longMeasurement = FakeObservableLongMeasurement()
        val doubleMeasurement = FakeObservableDoubleMeasurement()
        var calls = 0

        val registration = meter.batchCallback(longMeasurement, doubleMeasurement) {
            calls++
        }
        val fakeRegistration = meter.batchCallbacks.single()
        assertSame(registration, fakeRegistration)
        assertEquals(listOf(longMeasurement, doubleMeasurement), fakeRegistration.measurements)

        fakeRegistration.collect()
        assertEquals(1, calls)

        registration.close()
        registration.close()
        assertTrue(meter.batchCallbacks.isEmpty())
        fakeRegistration.collect()
        assertEquals(1, calls)
    }
}
