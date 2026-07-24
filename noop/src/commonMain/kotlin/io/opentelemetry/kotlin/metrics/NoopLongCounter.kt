package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

internal object NoopLongCounter : LongCounter {

    override val name: String
        get() = "noop"

    override val unit: String? = null

    override val description: String? = null

    override fun enabled(): Boolean {
        return false
    }

    override fun add(
        value: Long,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
    }
}
