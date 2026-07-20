package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * An [Instrument] whose measurements are recorded inline with application code.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#synchronous-and-asynchronous-instruments
 */
@ExperimentalApi
@ThreadSafe
public interface SynchronousInstrument : Instrument {

    /**
     * Returns whether this instrument is currently enabled.
     *
     * The result may change over time: instrumentation should call this each time a measurement
     * is recorded, immediately before performing expensive work needed only to produce the
     * measurement.
     */
    public fun isEnabled(): Boolean
}
