package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator

internal object NoopIntegerCounter: IntegerCounter {
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