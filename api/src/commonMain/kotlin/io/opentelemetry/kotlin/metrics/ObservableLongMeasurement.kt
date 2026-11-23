package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator

@ExperimentalApi
@ThreadSafe
public interface ObservableLongMeasurement : ObservableMeasurement {

    /**
     * Records a measurement, optionally associating attributes with it.
     */
    public fun record(value: Long, attributes: (AttributesMutator.() -> Unit)? = null)
}
