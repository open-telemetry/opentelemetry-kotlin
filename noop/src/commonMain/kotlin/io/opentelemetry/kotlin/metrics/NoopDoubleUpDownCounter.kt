package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * No-op implementation of [DoubleUpDownCounter].
 */
@ExperimentalApi
internal class NoopDoubleUpDownCounter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : DoubleUpDownCounter {
    override fun enabled(): Boolean = false
    override fun add(value: Double, attributes: AttributesMutator.() -> Unit) {}
}
