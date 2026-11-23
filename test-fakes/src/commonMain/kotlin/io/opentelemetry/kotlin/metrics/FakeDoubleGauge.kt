package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator

class FakeDoubleGauge(
    override val name: String,
    override val unit: String? = null,
    override val description: String? = null,
    var enabledResult: () -> Boolean = { true },
) : DoubleGauge {
    val records: MutableList<Pair<Double, Map<String, Any>>> = mutableListOf()

    override fun enabled(): Boolean = enabledResult()

    override fun record(value: Double, attributes: (AttributesMutator.() -> Unit)?) {
        records.add(value to FakeAttributesMutator().apply { attributes?.invoke(this) }.attributes)
    }
}
