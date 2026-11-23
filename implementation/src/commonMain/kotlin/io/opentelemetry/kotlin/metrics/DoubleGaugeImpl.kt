package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator

internal class DoubleGaugeImpl(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : DoubleGauge {
    override fun enabled(): Boolean = true
    override fun record(value: Double, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
