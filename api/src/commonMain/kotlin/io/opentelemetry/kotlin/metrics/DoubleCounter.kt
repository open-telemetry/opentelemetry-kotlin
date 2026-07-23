package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context


@OptIn(ExperimentalApi::class)
public interface DoubleCounter: SynchronousInstrument {


    /**
     * @param value Must be non-negative
     */
    public fun add(value: Double, context: Context?, attributes: (AttributesMutator.() -> Unit)? = null)
}