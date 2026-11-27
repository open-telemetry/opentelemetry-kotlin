package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * No-op implementation of [DoubleUpDownCounter].
 */
@OptIn(ExperimentalApi::class)
internal object NoopDoubleUpDownCounter : DoubleUpDownCounter {
    override val name: String = ""
    override val unit: String? = null
    override val description: String? = null

    override fun enabled(): Boolean = false
    override fun add(value: Double, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
