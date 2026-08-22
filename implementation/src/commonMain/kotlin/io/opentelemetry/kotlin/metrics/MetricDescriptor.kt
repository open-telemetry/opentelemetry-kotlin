package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor
import io.opentelemetry.kotlin.metrics.view.AttributesProcessor
import io.opentelemetry.kotlin.metrics.view.View

/**
 * SDK configuration for a metric stream after applying an instrument-matching View.
 *
 * One instrument may produce multiple independently configured streams when multiple Views match.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/sdk/#measurement-processing
 */
internal class MetricDescriptor private constructor(
    val name: String,
    val description: String?,
    val sourceInstrument: InstrumentDescriptor,
    val view: View,
    val attributesProcessor: AttributesProcessor,
    val cardinalityLimit: Int,
) {
    val unit: String? = sourceInstrument.unit

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is MetricDescriptor &&
            name.equals(other.name, ignoreCase = true) &&
            description == other.description &&
            sourceInstrument == other.sourceInstrument &&
            view.aggregation == other.view.aggregation &&
            attributesProcessor == other.attributesProcessor &&
            cardinalityLimit == other.cardinalityLimit

    override fun hashCode(): Int {
        var result = name.lowercase().hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + sourceInstrument.hashCode()
        result = 31 * result + view.aggregation.hashCode()
        result = 31 * result + attributesProcessor.hashCode()
        result = 31 * result + cardinalityLimit
        return result
    }

    override fun toString(): String =
        "MetricDescriptor(name=$name, description=$description, sourceInstrument=$sourceInstrument, " +
            "view=$view, cardinalityLimit=$cardinalityLimit)"

    companion object {
        fun create(
            sourceInstrument: InstrumentDescriptor,
            view: View,
            attributesProcessor: AttributesProcessor = view.attributesProcessor,
            cardinalityLimit: Int = view.cardinalityLimit,
        ): MetricDescriptor = MetricDescriptor(
            name = view.name ?: sourceInstrument.name,
            description = view.description ?: sourceInstrument.description,
            sourceInstrument = sourceInstrument,
            view = view,
            attributesProcessor = attributesProcessor,
            cardinalityLimit = cardinalityLimit,
        )
    }
}
