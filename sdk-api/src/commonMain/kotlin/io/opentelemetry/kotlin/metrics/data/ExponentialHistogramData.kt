package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Exponential histogram points that share an [aggregationTemporality]. Their bucket boundaries
 * are calculated from a base-2 exponential mapping rather than transmitted explicitly.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#exponentialhistogram
 */
@ExperimentalApi
@ThreadSafe
public interface ExponentialHistogramData : Data<ExponentialHistogramPoint> {
    public val aggregationTemporality: AggregationTemporality
}
