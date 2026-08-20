package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator

class FakeLongUpDownCounter(
    override val name: String,
    override val unit: String? = null,
    override val description: String? = null,
    var enabledResult: () -> Boolean = { true },
) : LongUpDownCounter {

    val adds: MutableList<Pair<Long, Map<String, Any>>> = mutableListOf()

    override fun enabled(): Boolean = enabledResult()

    override fun add(value: Long) {
        adds.add(value to emptyMap())
    }

    override fun add(value: Long, attributes: AttributesMutator.() -> Unit) {
        adds.add(value to FakeAttributesMutator().apply(attributes).attributes)
    }
}
