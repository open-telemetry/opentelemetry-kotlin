package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Periodic reader settings, in milliseconds where applicable. Each null field is unset and defers
 * to a lower configuration layer or the SDK default.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metricreader
 *
 * Metric producers and cardinality limits are separate configuration and are not represented here.
 */
@OptIn(ExperimentalApi::class)
data class PeriodicMetricReaderBehavior(
    /** Delay between consecutive exports, in milliseconds. Must be non-negative. */
    val interval: Long? = null,
    /** Maximum export duration, in milliseconds. Zero means no limit. Must be non-negative. */
    val timeout: Long? = null,
    /** Maximum data points per export. Must be positive. Null means no limit. */
    val maxExportBatchSize: Int? = null,
    /** Exporter that receives each collection. */
    val exporter: PushMetricExporterBehavior? = null,
) : Behavior<PeriodicMetricReaderBehavior> {
    override fun mergeWith(higher: PeriodicMetricReaderBehavior): PeriodicMetricReaderBehavior = copy(
        interval = higher.interval ?: interval,
        timeout = higher.timeout ?: timeout,
        maxExportBatchSize = higher.maxExportBatchSize ?: maxExportBatchSize,
        exporter = mergeNode(exporter, higher.exporter),
    )
}
