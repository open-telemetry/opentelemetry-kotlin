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
public interface AsynchronousInstrument : Instrument, AutoCloseable {
    // TODO after-creation registerCallback method to be added
}
