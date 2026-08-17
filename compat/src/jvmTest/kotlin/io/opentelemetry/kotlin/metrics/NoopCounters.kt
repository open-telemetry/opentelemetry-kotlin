package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

internal object NoopTestLongCounter : LongCounter {

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

internal object NoopTestIntegerCounter : IntegerCounter {

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

internal object NoopTestFloatCounter : FloatCounter {

    override val name: String
        get() = "noop"

    override val unit: String? = null

    override val description: String? = null

    override fun enabled(): Boolean {
        return false
    }

    override fun add(
        value: Float,
        context: Context?,
        attributes: (AttributesMutator.() -> Unit)?
    ) {
    }
}

internal object NoopTestDoubleCounter : DoubleCounter {

    override val name: String
        get() = "noop"

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
