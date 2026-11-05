package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

@ExperimentalApi
internal class NoopLongGauge(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : LongGauge {
    override fun enabled(): Boolean = false
    override fun record(value: Long, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
