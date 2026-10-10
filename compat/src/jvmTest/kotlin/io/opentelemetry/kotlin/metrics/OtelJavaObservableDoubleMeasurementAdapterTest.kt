package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.attributes.attrsFromMap
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class OtelJavaObservableDoubleMeasurementAdapterTest {

    @Test
    fun recordWithoutAttributesDelegatesToKotlin() {
        val recordingKotlinDoubleMeasurement = RecordingKotlinDoubleMeasurement()
        val adapter = OtelJavaObservableDoubleMeasurementAdapter(impl = recordingKotlinDoubleMeasurement)

        adapter.record(4.2)
        assertEquals(listOf(4.2 to emptyMap()), recordingKotlinDoubleMeasurement.adds)
    }

    @Test
    fun recordWithAttributesDelegatesToKotlin() {
        val recordingKotlinDoubleMeasurement = RecordingKotlinDoubleMeasurement()
        val adapter = OtelJavaObservableDoubleMeasurementAdapter(impl = recordingKotlinDoubleMeasurement)

        adapter.record(4.2, attrsFromMap(mapOf("color" to "red")))
        assertEquals(listOf(4.2 to mapOf<String, Any>("color" to "red")), recordingKotlinDoubleMeasurement.adds)
    }

    @Test
    fun recordWithEmptyAttributesDelegatesToKotlin() {
        val recordingKotlinDoubleMeasurement = RecordingKotlinDoubleMeasurement()
        val adapter = OtelJavaObservableDoubleMeasurementAdapter(impl = recordingKotlinDoubleMeasurement)

        adapter.record(4.2, OtelJavaAttributes.empty())
        assertEquals(listOf(4.2 to emptyMap()), recordingKotlinDoubleMeasurement.adds)
    }

    private class RecordingKotlinDoubleMeasurement() : ObservableDoubleMeasurement {
        val adds: MutableList<Pair<Double, Map<String, Any>>> = mutableListOf()
        override fun record(value: Double) {
            record(value) {}
        }

        override fun record(
            value: Double,
            attributes: AttributesMutator.() -> Unit,
        ) {
            adds.add(value to FakeAttributesMutator().apply(attributes).attributes)
        }
    }
}
