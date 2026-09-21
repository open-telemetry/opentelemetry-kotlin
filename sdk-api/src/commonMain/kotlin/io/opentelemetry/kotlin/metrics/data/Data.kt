package io.opentelemetry.kotlin.metrics.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * The points in a metric stream, with at most one point for each distinct attribute set in a
 * collection cycle.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/data-model/#opentelemetry-protocol-data-model
 */
@ExperimentalApi
@ThreadSafe
public sealed interface Data<out T : PointData> {
    public val points: List<T>
}
