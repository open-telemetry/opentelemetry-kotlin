package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

internal object NoopDoubleCounter : DoubleCounter {

    override val name: String = "noop"

    override val unit: String? = null

    override val description: String? = null

    override fun enabled(): Boolean {
        return false
    }

    override fun add(
        value: Double,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
    }
}
