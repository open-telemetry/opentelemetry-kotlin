package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class FakeObservableDoubleMeasurementTest {

    @Test
    fun doubleRecordWithoutAttributes() {
        val doubleMeasurement = FakeObservableDoubleMeasurement()

        doubleMeasurement.record(4.2)

        assertEquals(listOf(4.2 to emptyMap()), doubleMeasurement.adds)
    }

    @Test
    fun doubleRecordWithAttributes() {
        val doubleMeasurement = FakeObservableDoubleMeasurement()

        doubleMeasurement.record(4.2) { setStringAttribute("color", "red") }

        assertEquals(listOf(4.2 to mapOf<String, Any>("color" to "red")), doubleMeasurement.adds)
    }

    @Test
    fun doubleRecordsPreserveOrderAndIndependentAttributes() {
        val doubleMeasurement = FakeObservableDoubleMeasurement()

        doubleMeasurement.record(4.2) { setStringAttribute("color", "red") }
        doubleMeasurement.record(9.9)

        assertEquals(listOf(4.2 to mapOf<String, Any>("color" to "red"), 9.9 to emptyMap()), doubleMeasurement.adds)
    }
}
