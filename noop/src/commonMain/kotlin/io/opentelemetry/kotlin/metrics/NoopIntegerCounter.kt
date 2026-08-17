package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

internal object NoopIntegerCounter : IntegerCounter {

    override val name: String
        get() = "noop"

    override val unit: String? = null

    override val description: String? = null

    override fun enabled(): Boolean {
        return false
    }

    override fun add(
        value: UInt,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
    }
}
