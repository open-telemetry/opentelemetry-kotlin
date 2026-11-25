package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * How collected metrics leave the SDK. A reader is either periodic or pull.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metricreader
 */
@OptIn(ExperimentalApi::class)
data class MetricReaderBehavior(
    /**
     * Reader that collects on an interval and pushes to an exporter.
     */
    val periodic: PeriodicMetricReaderBehavior? = null,
    /**
     * Reader that collects when a pull exporter asks for metrics.
     */
    val pull: PullMetricReaderBehavior? = null,
) : Behavior<MetricReaderBehavior> {
    override fun mergeWith(higher: MetricReaderBehavior): MetricReaderBehavior = copy(
        periodic = mergeNode(periodic, higher.periodic),
        pull = mergeNode(pull, higher.pull),
    )
}
