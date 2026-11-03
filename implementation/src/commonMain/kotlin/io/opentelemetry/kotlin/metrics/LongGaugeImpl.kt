package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator

internal class LongGaugeImpl(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : LongGauge {
    override fun enabled(): Boolean = true
    override fun record(value: Long, attributes: AttributesMutator.() -> Unit) = Unit
}
