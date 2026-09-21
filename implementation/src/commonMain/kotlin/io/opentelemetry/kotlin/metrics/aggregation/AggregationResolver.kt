package io.opentelemetry.kotlin.metrics.aggregation

import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor

/**
 * Internal strategy for resolving a MetricReader's concrete default aggregation for an
 * instrument, including relevant instrument advice.
 *
 * This resolver is an implementation-specific abstraction, not an entity defined by the
 * OpenTelemetry Metrics SDK. It separates reader-specific default selection from View and metric
 * stream resolution.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#default-aggregation
 */
internal fun interface AggregationResolver {
    fun resolve(instrument: InstrumentDescriptor): Aggregation
}
