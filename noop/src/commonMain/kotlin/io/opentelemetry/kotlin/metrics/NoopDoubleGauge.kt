package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

@ExperimentalApi
internal class NoopDoubleGauge(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : DoubleGauge {
    override fun enabled(): Boolean = false
    override fun record(value: Double, attributes: AttributesMutator.() -> Unit) = Unit
}
