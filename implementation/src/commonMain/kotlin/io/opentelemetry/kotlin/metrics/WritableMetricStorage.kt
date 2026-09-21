package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context

/**
 * Records synchronous measurements together with attributes and the Context used for exemplar
 * sampling.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#synchronous-instrument-api
 */
internal interface WritableMetricStorage {
    val isEnabled: Boolean

    fun recordLong(
        value: Long,
        attributes: AttributesModel,
        context: Context,
    )

    fun recordDouble(
        value: Double,
        attributes: AttributesModel,
        context: Context,
    )
}
