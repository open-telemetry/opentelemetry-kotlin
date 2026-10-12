package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ObservableLongMeasurementAdapterTest {

    @Test
    fun recordWithoutAttributesDelegatesToJava() {
        val javaMeasurement = RecordingJavaLongMeasurement()
        val adapter = ObservableLongMeasurementAdapter(javaMeasurement)
        adapter.record(42L)
        adapter.record(-42L)
        adapter.record(0L)
        assertEquals(
            listOf(
                42L to emptyMap(),
                -42L to emptyMap(),
                0L to emptyMap(),
            ),
            javaMeasurement.adds
        )
    }

    @Test
    fun recordWithAttributesDelegatesToJava() {
        val javaMeasurement = RecordingJavaLongMeasurement()
        val adapter = ObservableLongMeasurementAdapter(javaMeasurement)
        adapter.record(42L) { setStringAttribute("color", "red") }
        assertEquals(
            listOf(
                42L to mapOf<String, Any>("color" to "red"),
            ),
            javaMeasurement.adds
        )
    }

    private class RecordingJavaLongMeasurement : OtelJavaObservableLongMeasurement {
        val adds = mutableListOf<Pair<Long, Map<String, Any>>>()

        override fun record(value: Long) {
            adds.add(value to emptyMap())
        }

        override fun record(value: Long, attributes: OtelJavaAttributes) {
            adds.add(value to attributes.asMap().mapKeys { it.key.key })
        }
    }
}
