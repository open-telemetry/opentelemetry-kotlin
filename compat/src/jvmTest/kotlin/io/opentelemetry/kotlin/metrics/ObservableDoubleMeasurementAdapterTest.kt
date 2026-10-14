package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableDoubleMeasurementAdapterTest {

    @Test
    fun recordWithoutAttributesDelegatesToJava() {
        val javaMeasurement = RecordingJavaDoubleMeasurement()
        val adapter = ObservableDoubleMeasurementAdapter(javaMeasurement)
        adapter.record(4.2)
        adapter.record(-4.2)
        adapter.record(0.0)
        assertEquals(
            listOf(
                4.2 to emptyMap(),
                -4.2 to emptyMap(),
                0.0 to emptyMap(),
            ),
            javaMeasurement.adds
        )
    }

    @Test
    fun recordWithAttributesDelegatesToJava() {
        val javaMeasurement = RecordingJavaDoubleMeasurement()
        val adapter = ObservableDoubleMeasurementAdapter(javaMeasurement)
        adapter.record(4.2) { setStringAttribute("color", "red") }
        assertEquals(
            listOf(
                4.2 to mapOf<String, Any>("color" to "red"),
            ),
            javaMeasurement.adds
        )
    }

    private class RecordingJavaDoubleMeasurement : OtelJavaObservableDoubleMeasurement {
        val adds = mutableListOf<Pair<Double, Map<String, Any>>>()

        override fun record(value: Double) {
            adds.add(value to emptyMap())
        }

        override fun record(value: Double, attributes: OtelJavaAttributes) {
            adds.add(value to attributes.asMap().mapKeys { it.key.key })
        }
    }
}
