package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Pull reader: collection runs when the exporter serves a scrape.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metricreader
 *
 * Metric producers and cardinality limits are separate configuration and are not represented here.
 */
@OptIn(ExperimentalApi::class)
data class PullMetricReaderBehavior(
    /** Exporter that pulls metrics from the SDK. */
    val exporter: PullMetricExporterBehavior? = null,
) : Behavior<PullMetricReaderBehavior> {
    override fun mergeWith(higher: PullMetricReaderBehavior): PullMetricReaderBehavior = copy(
        exporter = mergeNode(exporter, higher.exporter),
    )
}
