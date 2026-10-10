package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class FakeObservableLongMeasurementTest {

    @Test
    fun longRecordWithoutAttributes() {
        val longMeasurement = FakeObservableLongMeasurement()

        longMeasurement.record(42L)

        assertEquals(listOf(42L to emptyMap()), longMeasurement.adds)
    }

    @Test
    fun longRecordWithAttributes() {
        val longMeasurement = FakeObservableLongMeasurement()

        longMeasurement.record(42L) { setStringAttribute("color", "red") }

        assertEquals(listOf(42L to mapOf<String, Any>("color" to "red")), longMeasurement.adds)
    }

    @Test
    fun longRecordsPreserveOrderAndIndependentAttributes() {
        val longMeasurement = FakeObservableLongMeasurement()

        longMeasurement.record(42L) { setStringAttribute("color", "red") }
        longMeasurement.record(99L)

        assertEquals(listOf(42L to mapOf<String, Any>("color" to "red"), 99L to emptyMap()), longMeasurement.adds)
    }
}
