package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * An [Instrument] whose measurements are reported by callback functions invoked on demand
 * during metric collection. Measurements reported by asynchronous instruments cannot be
 * associated with a context.
 *
 * Calling [close] unregisters the callbacks associated with this instrument, after which they
 * are no longer invoked.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#synchronous-and-asynchronous-instruments
 */
@ExperimentalApi
@ThreadSafe
public interface AsynchronousInstrument<out M : ObservableMeasurement> : Instrument, AutoCloseable {

    /**
     * Registers a callback that reports measurements for this instrument.
     *
     * The callback is invoked when metrics are collected. It should be reentrant-safe and should
     * not take an indefinite amount of time.
     *
     * Closing the returned handle unregisters this callback. Closing this instrument unregisters
     * all callbacks registered with it.
     *
     * @param callback callback used to report measurements for this instrument
     * @return a handle that unregisters this callback when closed
     */
    public fun registerCallback(callback: (M) -> Unit): AutoCloseable
}
