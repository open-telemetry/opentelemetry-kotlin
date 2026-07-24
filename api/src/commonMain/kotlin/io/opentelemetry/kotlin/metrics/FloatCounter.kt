package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

@OptIn(ExperimentalApi::class)
public interface FloatCounter : SynchronousInstrument {

    /**
     * @param value Must be non-negative
     */
    public fun add(value: Float, context: Context?, attributes: (AttributesMutator.() -> Unit)? = null)
}
