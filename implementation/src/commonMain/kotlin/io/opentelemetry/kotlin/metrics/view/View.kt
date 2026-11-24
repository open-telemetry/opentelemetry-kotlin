package io.opentelemetry.kotlin.metrics.view

import io.opentelemetry.kotlin.metrics.aggregation.Aggregation

/**
 * Instrument selection criteria and the stream configuration applied to every matching
 * instrument independently of other Views.
 *
 * Settings left unset fall back when the metric stream is resolved:
 * - [name] and [description] to the instrument's values.
 * - [attributesProcessor] to the instrument's attribute advice, keeping every attribute when
 *   there is no advice.
 * - [aggregation] and [cardinalityLimit] to the MetricReader's defaults. A non-positive
 *   [cardinalityLimit] is treated as unset.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#view
 */
internal data class View(
    val selector: InstrumentSelector,
    val name: String? = null,
    val description: String? = null,
    val aggregation: Aggregation? = null,
    val attributesProcessor: AttributesProcessor? = null,
    val cardinalityLimit: Int? = null,
)
