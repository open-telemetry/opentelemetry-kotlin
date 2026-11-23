package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior

/**
 * Prometheus exporter endpoint. Each null field is unset and defers to a lower layer or the
 * exporter default.
 */
@OptIn(ExperimentalApi::class)
data class PrometheusMetricExporterBehavior(
    /** Host the scrape endpoint binds to. */
    val host: String? = null,
    /** Port the scrape endpoint binds to. */
    val port: Int? = null,
) : Behavior<PrometheusMetricExporterBehavior> {
    override fun mergeWith(higher: PrometheusMetricExporterBehavior): PrometheusMetricExporterBehavior = copy(
        host = higher.host ?: host,
        port = higher.port ?: port,
    )
}
