package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator


@OptIn(ExperimentalApi::class)
public interface FloatCounter {

    public fun getName(): String

    public fun getUnit(): String?
    public fun getDescription(): String?
    public fun isEnabled(): Boolean

    /**
     * @param value Must be non-negative
     */
    public fun add(value: Float, attributes: (AttributesMutator.() -> Unit)? = null)
}