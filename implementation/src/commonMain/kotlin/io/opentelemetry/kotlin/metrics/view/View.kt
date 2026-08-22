package io.opentelemetry.kotlin.metrics.view

import io.opentelemetry.kotlin.metrics.MetricStorage
import io.opentelemetry.kotlin.metrics.aggregation.Aggregation

/**
 * Instrument selection criteria and the stream configuration applied to every matching
 * instrument independently of other Views.
 *
 * An explicitly configured stream setting overrides advice for the same aspect. A null
 * [aggregation] delegates aggregation selection to the MetricReader default.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#view
 */
internal data class View(
    val selector: InstrumentSelector,
    val name: String? = null,
    val description: String? = null,
    val aggregation: Aggregation? = null,
    val attributesProcessor: AttributesProcessor = AttributesProcessor.Noop,
    val cardinalityLimit: Int = MetricStorage.DEFAULT_MAX_CARDINALITY,
) {
    init {
        require(cardinalityLimit > 0) { "cardinalityLimit must be positive" }
    }
}
