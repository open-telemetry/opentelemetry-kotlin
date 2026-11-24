package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Pull exporters for a pull metric reader.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#pull-metric-exporter
 */
@OptIn(ExperimentalApi::class)
data class PullMetricExporterBehavior(
    /** Prometheus scrape endpoint. */
    val prometheus: PrometheusMetricExporterBehavior? = null,
) : Behavior<PullMetricExporterBehavior> {
    override fun mergeWith(higher: PullMetricExporterBehavior): PullMetricExporterBehavior = copy(
        prometheus = mergeNode(prometheus, higher.prometheus),
    )
}
