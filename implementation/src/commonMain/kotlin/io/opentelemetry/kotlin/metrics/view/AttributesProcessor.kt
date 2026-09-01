package io.opentelemetry.kotlin.metrics.view

import io.opentelemetry.kotlin.attributes.AttributesModel

/**
 * Applies a View's attribute filtering before attributes form an aggregation key.
 *
 * Cardinality limits are evaluated after this processing so excluded attributes cannot create
 * additional metric points.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#stream-configuration
 */
internal sealed interface AttributesProcessor {

    fun process(attributes: AttributesModel): AttributesModel

    data object Noop : AttributesProcessor {
        override fun process(attributes: AttributesModel): AttributesModel = attributes
    }
}
