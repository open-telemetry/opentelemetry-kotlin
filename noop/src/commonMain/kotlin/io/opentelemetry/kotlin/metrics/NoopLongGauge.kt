package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

@OptIn(ExperimentalApi::class)
internal object NoopLongGauge : LongGauge {
    override val name: String = ""
    override val unit: String? = null
    override val description: String? = null

    override fun enabled(): Boolean = false
    override fun record(value: Long, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
