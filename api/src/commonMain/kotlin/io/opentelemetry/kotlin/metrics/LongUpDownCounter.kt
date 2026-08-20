package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * A synchronous [Instrument] which supports increments and decrements.
 *
 * If the value is monotonically increasing, use a Counter instead.
 *
 * Example uses: the number of active requests, the number of items in a queue.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#updowncounter
 */
@ExperimentalApi
@ThreadSafe
public interface LongUpDownCounter : SynchronousInstrument {

    /**
     * Increments or decrements this counter by [value].
     *
     * [value] may be positive, negative, or zero.
     */
    @ThreadSafe
    public fun add(value: Long)

    /**
     * Increments or decrements this counter by [value], associating [attributes] with the
     * measurement.
     *
     * [value] may be positive, negative, or zero.
     */
    @ThreadSafe
    public fun add(value: Long, attributes: AttributesMutator.() -> Unit)
}
