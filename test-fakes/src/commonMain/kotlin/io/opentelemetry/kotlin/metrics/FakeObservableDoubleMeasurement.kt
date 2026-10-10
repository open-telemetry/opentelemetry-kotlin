package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator

class FakeObservableDoubleMeasurement : ObservableDoubleMeasurement {

    val adds: MutableList<Pair<Double, Map<String, Any>>> = mutableListOf()

    override fun record(value: Double) {
        record(value) {}
    }

    override fun record(value: Double, attributes: AttributesMutator.() -> Unit) {
        adds.add(value to FakeAttributesMutator().apply(attributes).attributes)
    }
}
