package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

class FakeLongCounter: LongCounter {

    override fun add(
        value: Long,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {

    }

    override fun enabled(): Boolean {
        return true
    }

    override val name: String
        get() = "fake"
    override val unit: String?
        get() = null
    override val description: String?
        get() = null
}
