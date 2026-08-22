package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * A population of measurements aggregated into base-2 exponential buckets over this point's
 * time interval.
 *
 * Bucket boundaries are determined by [scale], while positive and negative measurements are
 * recorded separately in [positiveBuckets] and [negativeBuckets].
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#exponentialhistogram
 */
@ExperimentalApi
@ThreadSafe
public interface ExponentialHistogramPoint : PointData {

    /**
     * Resolution used to derive the histogram base as `2 ** (2 ** -scale)`; larger values
     * provide greater precision.
     */
    public val scale: Int

    public val count: Long

    /** Sum of represented measurements, or `null` when the aggregation did not collect it. */
    public val sum: Double?

    /** Minimum represented measurement, or `null` when min was not collected. */
    public val min: Double?

    /** Maximum represented measurement, or `null` when max was not collected. */
    public val max: Double?

    /** Number of measurements whose absolute value is at most [zeroThreshold]. */
    public val zeroCount: Long

    /** Inclusive absolute-value threshold used by the zero bucket. */
    public val zeroThreshold: Double

    public val positiveBuckets: ExponentialHistogramBuckets

    /** Negative measurements bucketed by absolute value. */
    public val negativeBuckets: ExponentialHistogramBuckets
}
