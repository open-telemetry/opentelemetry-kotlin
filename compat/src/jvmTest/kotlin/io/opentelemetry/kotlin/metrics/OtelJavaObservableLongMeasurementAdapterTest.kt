package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator
import io.opentelemetry.kotlin.attributes.attrsFromMap
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class OtelJavaObservableLongMeasurementAdapterTest {

    @Test
    fun recordWithoutAttributesDelegatesToKotlin() {
        val recordingKotlinLongMeasurement = RecordingKotlinLongMeasurement()
        val adapter = OtelJavaObservableLongMeasurementAdapter(impl = recordingKotlinLongMeasurement)

        adapter.record(42L)
        assertEquals(listOf(42L to emptyMap()), recordingKotlinLongMeasurement.adds)
    }

    @Test
    fun recordWithAttributesDelegatesToKotlin() {
        val recordingKotlinLongMeasurement = RecordingKotlinLongMeasurement()
        val adapter = OtelJavaObservableLongMeasurementAdapter(impl = recordingKotlinLongMeasurement)

        adapter.record(42L, attrsFromMap(mapOf("color" to "red")))
        assertEquals(listOf(42L to mapOf<String, Any>("color" to "red")), recordingKotlinLongMeasurement.adds)
    }

    @Test
    fun recordWithEmptyAttributesDelegatesToKotlin() {
        val recordingKotlinLongMeasurement = RecordingKotlinLongMeasurement()
        val adapter = OtelJavaObservableLongMeasurementAdapter(impl = recordingKotlinLongMeasurement)

        adapter.record(42L, OtelJavaAttributes.empty())
        assertEquals(listOf(42L to emptyMap()), recordingKotlinLongMeasurement.adds)
    }

    private class RecordingKotlinLongMeasurement() : ObservableLongMeasurement {
        val adds: MutableList<Pair<Long, Map<String, Any>>> = mutableListOf()
        override fun record(value: Long) {
            record(value) {}
        }

        override fun record(
            value: Long,
            attributes: AttributesMutator.() -> Unit,
        ) {
            adds.add(value to FakeAttributesMutator().apply(attributes).attributes)
        }
    }
}
