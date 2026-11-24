package io.opentelemetry.kotlin.behavior.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior

/**
 * Behavior of metrics.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#meterprovider
 */
@OptIn(ExperimentalApi::class)
data class MeterProviderBehavior(
    /**
     * Readers that collect metrics from the SDK.
     *
     * A non-null list replaces the lower layer's list. An empty list means this layer configured
     * no readers.
     */
    val readers: List<MetricReaderBehavior>? = null,
) : Behavior<MeterProviderBehavior> {
    override fun mergeWith(higher: MeterProviderBehavior): MeterProviderBehavior = copy(
        readers = higher.readers ?: readers,
    )
}
