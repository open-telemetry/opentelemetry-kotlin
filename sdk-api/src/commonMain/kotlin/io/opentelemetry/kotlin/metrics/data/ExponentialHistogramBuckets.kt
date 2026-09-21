package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * A dense range of positive or negative base-2 exponential histogram buckets.
 *
 * Element `i` of [bucketCounts] records the count for bucket index `offset + i`.
 * Bucket boundaries are derived from that index and the containing point's scale.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#exponential-buckets
 */
@ExperimentalApi
@ThreadSafe
public interface ExponentialHistogramBuckets {

    /** Index of the first bucket represented by [bucketCounts]. */
    public val offset: Int

    /** Counts for the contiguous bucket indexes beginning at [offset]. */
    public val bucketCounts: List<Long>
}
