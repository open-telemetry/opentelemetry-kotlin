package io.opentelemetry.kotlin.metrics.instrument

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Language-level numeric type recorded by an instrument.
 *
 * Numeric type participates in instrument identity even though integer and floating-point values
 * do not create distinct OTLP metric-stream identities.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument
 */
@ExperimentalApi
public enum class InstrumentValueType {
    DOUBLE,
    LONG,
}
