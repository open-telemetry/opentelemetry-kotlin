package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator

internal class LongUpDownCounterImpl(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : LongUpDownCounter {
    override fun enabled(): Boolean = true
    override fun add(value: Long, attributes: AttributesMutator.() -> Unit) {}
}
