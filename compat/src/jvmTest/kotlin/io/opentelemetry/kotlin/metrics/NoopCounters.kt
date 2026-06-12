package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator

internal object NoopTestLongCounter: LongCounter {
    override fun getName(): String {
        return "noop"
    }

    override fun getUnit(): String? {
        return null
    }

    override fun getDescription(): String? {
        return null
    }

    override fun isEnabled(): Boolean {
        return false
    }

    override fun add(
        value: Long,
        attributes: (AttributesMutator.() -> Unit)?
    ) {

    }
}

internal object NoopTestIntegerCounter: IntegerCounter {
    override fun getName(): String {
        return "noop"
    }

    override fun getUnit(): String? {
        return null
    }

    override fun getDescription(): String? {
        return null
    }

    override fun isEnabled(): Boolean {
        return false
    }

    override fun add(
        value: UInt,
        attributes: (AttributesMutator.() -> Unit)?
    ) {

    }
}


internal object NoopTestFloatCounter: FloatCounter {
    override fun getName(): String {
        return "noop"
    }

    override fun getUnit(): String? {
        return null
    }

    override fun getDescription(): String? {
        return null
    }

    override fun isEnabled(): Boolean {
        return false
    }

    override fun add(
        value: Float,
        attributes: (AttributesMutator.() -> Unit)?
    ) {

    }
}

internal object NoopTestDoubleCounter: DoubleCounter {
    override fun getName(): String {
        return "noop"
    }

    override fun getUnit(): String? {
        return null
    }

    override fun getDescription(): String? {
        return null
    }

    override fun isEnabled(): Boolean {
        return false
    }

    override fun add(
        value: Double,
        attributes: (AttributesMutator.() -> Unit)?
    ) {

    }
}