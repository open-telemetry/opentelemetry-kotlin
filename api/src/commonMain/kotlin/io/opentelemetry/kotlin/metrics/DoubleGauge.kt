package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * A synchronous instrument for recording non-additive floating-point values.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#gauge
 */
@ExperimentalApi
@ThreadSafe
public interface DoubleGauge : SynchronousInstrument {
    /**
     * Records the current [value], optionally associating attributes with the measurement.
     */
    @ThreadSafe
    public fun record(value: Double, attributes: (AttributesMutator.() -> Unit)? = null)
}
