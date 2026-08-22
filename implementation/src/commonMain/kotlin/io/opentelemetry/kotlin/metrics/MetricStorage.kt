package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.metrics.data.MetricData
import io.opentelemetry.kotlin.resource.Resource

/**
 * Aggregation state for one resolved stream and MetricReader pipeline.
 *
 * Storage persistence across collections depends on instrument synchronicity and the reader's
 * selected temporality.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metricreader
 */
internal interface MetricStorage {
    val descriptor: MetricDescriptor

    /**
     * Collects the metric data currently retained by this storage.
     *
     * Delta storage advances its interval after collection; cumulative storage retains its start
     * timestamp and previously observed synchronous series.
     */
    fun collect(
        resource: Resource,
        instrumentationScopeInfo: InstrumentationScopeInfo,
        timestampEpochNanos: Long,
    ): MetricData

    fun setEnabled(enabled: Boolean)

    companion object {
        const val DEFAULT_MAX_CARDINALITY: Int = 2_000
    }
}
