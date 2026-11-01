package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * An asynchronous [Instrument] that reports monotonically increasing Double values when observed.
 *
 * Each callback reports absolute values, not increments.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#asynchronous-counter
 */
@ExperimentalApi
@ThreadSafe
public interface ObservableDoubleCounter : AsynchronousInstrument<ObservableDoubleMeasurement>
