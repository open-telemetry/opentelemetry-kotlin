package io.opentelemetry.kotlin.metrics.instrument

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Classification of a metric instrument used to capture measurements.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument
 */
@ExperimentalApi
public enum class InstrumentKind {
    HISTOGRAM,
}
