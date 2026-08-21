package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator

@ExperimentalApi
@ThreadSafe
public interface ObservableDoubleMeasurement : ObservableMeasurement {

    /**
     * Records a measurement.
     */
    public fun record(value: Double)

    /**
     * Records a measurement with a set of attributes.
     */
    public fun record(value: Double, attributes: AttributesMutator.() -> Unit)
}
