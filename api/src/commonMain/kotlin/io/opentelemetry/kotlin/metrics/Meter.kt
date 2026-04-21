package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Provides instruments used to record measurements which are aggregated to metrics.
 *
 * Instruments are obtained through methods provided by this interface.
 *
 * See the [instrument selection guidelines](https://opentelemetry.io/docs/specs/otel/metrics/supplementary-guidelines/#instrument-selection)
 * for help choosing the right instrument.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#meter
 */
@ExperimentalApi
@ThreadSafe
public interface Meter
