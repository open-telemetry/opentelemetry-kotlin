package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.FakeAttributesMutator

class FakeLongGauge(
    override val name: String,
    override val unit: String? = null,
    override val description: String? = null,
    var enabledResult: () -> Boolean = { true },
) : LongGauge {
    val records: MutableList<Pair<Long, Map<String, Any>>> = mutableListOf()

    override fun enabled(): Boolean = enabledResult()

    override fun record(value: Long, attributes: (AttributesMutator.() -> Unit)?) {
        records.add(value to FakeAttributesMutator().apply { attributes?.invoke(this) }.attributes)
    }
}
