package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * An asynchronous [Instrument] that reports monotonically increasing Long values when observed.
 *
 * Each callback reports absolute values, not increments.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#asynchronous-counter
 */
@ExperimentalApi
@ThreadSafe
public interface ObservableLongCounter : AsynchronousInstrument<ObservableLongMeasurement>
