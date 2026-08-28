package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * A population of measurements aggregated into explicit-boundary buckets over this point's
 * time interval.
 *
 * When bucket data is present for `N` boundaries, [counts] contains `N + 1` entries covering
 * `(-inf, boundaries[0]]`, each adjacent `(boundaries[i - 1], boundaries[i]]` interval, and
 * `(boundaries[N - 1], +inf)`. Empty [boundaries] and [counts] represent omitted buckets.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#histogram
 */
@ExperimentalApi
@ThreadSafe
public interface HistogramPoint : PointData {

    public val count: Long

    /** Sum of represented measurements, or `null` when the aggregation did not collect it. */
    public val sum: Double?

    /** Minimum represented measurement, or `null` when min was not collected. */
    public val min: Double?

    /** Maximum represented measurement, or `null` when max was not collected. */
    public val max: Double?

    /** Strictly increasing upper bounds; each bound is inclusive for its bucket. */
    public val boundaries: List<Double>

    public val counts: List<Long>
}
