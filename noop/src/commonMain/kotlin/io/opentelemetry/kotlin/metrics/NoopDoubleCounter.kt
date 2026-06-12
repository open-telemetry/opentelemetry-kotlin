package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesMutator

internal object NoopDoubleCounter: DoubleCounter {
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