package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator

internal class DoubleUpDownCounterImpl(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : DoubleUpDownCounter {
    override fun enabled(): Boolean = true
    override fun add(value: Double, attributes: AttributesMutator.() -> Unit) = Unit
}
