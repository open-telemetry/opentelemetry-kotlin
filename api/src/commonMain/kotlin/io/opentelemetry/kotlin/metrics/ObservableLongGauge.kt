package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * An asynchronous [Instrument] that reports non-additive Long values when observed.
 *
 * Each callback reports the current value.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#asynchronous-gauge
 */
@ExperimentalApi
@ThreadSafe
public interface ObservableLongGauge : AsynchronousInstrument<ObservableLongMeasurement>
