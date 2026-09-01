package io.opentelemetry.kotlin.metrics.aggregation

/**
 * A decomposable operation and its configuration overrides for converting measurements into
 * metric points.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#aggregation
 */
internal sealed interface Aggregation {
    val name: String
}
