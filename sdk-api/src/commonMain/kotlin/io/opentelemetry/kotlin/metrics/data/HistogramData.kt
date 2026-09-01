package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Histogram points that compress a population into count, sum, and optional explicit buckets over
 * intervals described by [aggregationTemporality].
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#histogram
 */
@ExperimentalApi
@ThreadSafe
public interface HistogramData : Data<HistogramPoint> {
    public val aggregationTemporality: AggregationTemporality
}
