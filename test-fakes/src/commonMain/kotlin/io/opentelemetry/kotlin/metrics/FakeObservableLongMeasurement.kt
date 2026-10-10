package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator

class FakeObservableLongMeasurement : ObservableLongMeasurement {

    val adds: MutableList<Pair<Long, Map<String, Any>>> = mutableListOf()

    override fun record(value: Long) {
        record(value) {}
    }

    override fun record(value: Long, attributes: AttributesMutator.() -> Unit) {
        adds.add(value to FakeAttributesMutator().apply(attributes).attributes)
    }
}
