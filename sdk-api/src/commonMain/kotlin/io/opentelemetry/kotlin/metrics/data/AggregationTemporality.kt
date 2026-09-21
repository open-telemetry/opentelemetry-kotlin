package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines how successive points relate to the measurements observed during their time intervals.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#temporality
 */
@ExperimentalApi
public enum class AggregationTemporality {

    /** Each point covers only measurements recorded since the previous collection. */
    DELTA,

    /** Each point covers measurements recorded since a fixed start time. */
    CUMULATIVE,
}
