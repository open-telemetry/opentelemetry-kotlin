package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

@OptIn(ExperimentalApi::class)
internal object NoopDoubleGauge : DoubleGauge {
    override val name: String = ""
    override val unit: String? = null
    override val description: String? = null

    override fun enabled(): Boolean = false
    override fun record(value: Double, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
