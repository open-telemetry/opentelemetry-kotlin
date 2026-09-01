package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * The last sampled measurement for each attribute set in a collection cycle.
 *
 * Gauge streams do not carry aggregation temporality because their points describe values at a
 * sampling time rather than additive changes over an interval.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#gauge
 */
@ExperimentalApi
@ThreadSafe
public sealed interface GaugeData<out T : PointData> : Data<T> {

    @ExperimentalApi
    @ThreadSafe
    public interface LongGaugeData : GaugeData<LongPoint>

    @ExperimentalApi
    @ThreadSafe
    public interface DoubleGaugeData : GaugeData<DoublePoint>
}
