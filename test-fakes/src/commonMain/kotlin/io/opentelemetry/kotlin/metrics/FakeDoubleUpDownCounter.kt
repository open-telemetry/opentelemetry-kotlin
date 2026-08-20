package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator

class FakeDoubleUpDownCounter(
    override val name: String,
    override val unit: String? = null,
    override val description: String? = null,
    var enabledResult: () -> Boolean = { true },
) : DoubleUpDownCounter {

    val adds: MutableList<Pair<Double, Map<String, Any>>> = mutableListOf()

    override fun enabled(): Boolean = enabledResult()

    override fun add(value: Double) {
        adds.add(value to emptyMap())
    }

    override fun add(value: Double, attributes: AttributesMutator.() -> Unit) {
        adds.add(value to FakeAttributesMutator().apply(attributes).attributes)
    }
}
