package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Additive points whose values are interpreted according to [aggregationTemporality].
 *
 * Monotonic sums model non-decreasing quantities such as request counts; non-monotonic sums can
 * increase or decrease.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#sums
 */
@ExperimentalApi
@ThreadSafe
public sealed interface SumData<out T : PointData> : Data<T> {

    /** Whether the sum is expected to increase monotonically. */
    public val isMonotonic: Boolean

    public val aggregationTemporality: AggregationTemporality

    @ExperimentalApi
    @ThreadSafe
    public interface LongSumData : SumData<LongPoint>

    @ExperimentalApi
    @ThreadSafe
    public interface DoubleSumData : SumData<DoublePoint>
}
