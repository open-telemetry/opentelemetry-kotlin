package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * No-op implementation of [LongUpDownCounter].
 */
@OptIn(ExperimentalApi::class)
internal class NoopLongUpDownCounter(
    override val name: String,
    override val unit: String?,
    override val description: String?,
) : LongUpDownCounter {
    override fun enabled(): Boolean = false
    override fun add(value: Long, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
