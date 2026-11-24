package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Push exporters for a periodic metric reader.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#push-metric-exporter
 */
@OptIn(ExperimentalApi::class)
data class PushMetricExporterBehavior(
    /** Console exporter. Choosing it is the whole configuration. */
    val console: ConsoleExporterBehavior? = null,
) : Behavior<PushMetricExporterBehavior> {
    override fun mergeWith(higher: PushMetricExporterBehavior): PushMetricExporterBehavior = copy(
        console = mergeNode(console, higher.console),
    )
}
