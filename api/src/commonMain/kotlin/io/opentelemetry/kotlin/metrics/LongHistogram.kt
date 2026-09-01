package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.context.Context

/**
 * A [SynchronousInstrument] that records arbitrary values likely to be statistically
 * meaningful (e.g. request durations, response payload sizes).
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#histogram
 */
@ExperimentalApi
@ThreadSafe
public interface LongHistogram : SynchronousInstrument {

    /**
     * Records a Long measurement. The value is expected to be non-negative.
     */
    public fun record(
        value: Long,
        context: Context? = null,
        attributes: AttributesMutator.() -> Unit = {},
    )
}
