package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.attributes.AttributeContainer

/**
 * A metric point for one attribute set, with timestamps describing when its value applies.
 *
 * For interval points, the covered range is `(startEpochNanos, epochNanos]`. The start timestamp
 * represents the earliest time at which a measurement contributing to the series could have been
 * recorded.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#metric-points
 */
@ExperimentalApi
@ThreadSafe
public sealed interface PointData : AttributeContainer {

    /** Start of the point's interval as Unix epoch nanoseconds, or zero when unspecified. */
    public val startEpochNanos: Long

    /** Time at which the point took effect as Unix epoch nanoseconds. */
    public val epochNanos: Long
}
